package com.example.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class BluetoothRfidManager(private val context: Context) {

    private val TAG = "BluetoothRfidManager"

    // Standard Bluetooth Serial Port Profile (SPP) UUID
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    // Connection states
    var isConnected by mutableStateOf(false)
        private set

    var connectedDeviceName by mutableStateOf<String?>(null)
        private set

    var isConnecting by mutableStateOf(false)
        private set

    var terminalLogs by mutableStateOf("Leitor RFID J4212U desconectado.\nAguardando conexão...")
        private set

    val pairedDevices = mutableStateListOf<BluetoothDevice>()

    // Exposed flow for scanned RFID tags
    private val _scannedTags = MutableSharedFlow<String>(replay = 0)
    val scannedTags = _scannedTags.asSharedFlow()

    private var socket: BluetoothSocket? = null
    private var connectionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        updatePairedDevices()
    }

    /**
     * Checks if Bluetooth permissions are granted based on SDK version
     */
    fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Updates the list of paired Bluetooth devices
     */
    @SuppressLint("MissingPermission")
    fun updatePairedDevices() {
        pairedDevices.clear()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            terminalLogs = "Bluetooth desativado ou não suportado no dispositivo."
            return
        }

        try {
            if (hasBluetoothPermission()) {
                val bonded = bluetoothAdapter.bondedDevices
                bonded?.let {
                    pairedDevices.addAll(it)
                }
                terminalLogs = "Bluetooth ativo. ${pairedDevices.size} dispositivos pareados encontrados."
            } else {
                terminalLogs = "Permissão Bluetooth pendente para listar dispositivos pareados."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing paired devices", e)
            terminalLogs = "Erro ao buscar pareamentos: ${e.localizedMessage}"
        }
    }

    /**
     * Connect to target ESP32 / J4212U Bluetooth peripheral
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        if (bluetoothAdapter == null) return
        if (isConnecting || isConnected) return

        isConnecting = true
        terminalLogs = "Conectando ao dispositivo $address...\n"

        connectionJob = scope.launch {
            try {
                // Find device
                val device = bluetoothAdapter.getRemoteDevice(address)
                connectedDeviceName = device.name ?: "Leitor RFID ESP32"

                // Create physical SPP Socket
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                
                // Cancel discovery before connecting as it is a heavy operation
                if (hasBluetoothPermission()) {
                    bluetoothAdapter.cancelDiscovery()
                }

                // Connect (blocks until connected or fails)
                socket?.connect()

                isConnected = true
                isConnecting = false
                terminalLogs = "CONECTADO ao $connectedDeviceName ($address) via SPP.\n" +
                        "Escutando fluxo de RFID UHF J4212U...\n"

                // Start reader thread loop
                readStreamLoop(socket?.inputStream)

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed", e)
                closeSocket()
                isConnected = false
                isConnecting = false
                connectedDeviceName = null
                terminalLogs = "Falha de conexão com o dispositivo.\nErro: ${e.localizedMessage}\nVerifique se o ESP32/J4212U está ligado."
            }
        }
    }

    /**
     * Read and physical parser loop for J4212U RFID frame patterns
     */
    private suspend fun readStreamLoop(inputStream: InputStream?) {
        if (inputStream == null) return
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var hexAccumulator = StringBuilder()

        while (isConnected) {
            try {
                if (inputStream.available() > 0) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        Log.d(TAG, "Bytes received: $bytesRead")
                        
                        // Parse stream for J4212U structures
                        processRawBytes(buffer, bytesRead, hexAccumulator)
                    }
                }
                delay(100) // Polling interval to avoid high CPU usage
            } catch (e: IOException) {
                Log.e(TAG, "Stream disconnected", e)
                terminalLogs = "Fluxo Bluetooth desconectado inesperadamente."
                disconnect()
                break
            }
        }
    }

    /**
     * Stream packet parser:
     * Handles both J4212U physical frames starting with 0xBB (active tag read code 0x22)
     * as well as standard ASCII HEX strings of 24 chars (UHF EPC standard) received over serial TX path.
     */
    private suspend fun processRawBytes(buffer: ByteArray, length: Int, hexAccumulator: StringBuilder) {
        // Log raw string/hex chunk received
        val hexString = buffer.take(length).joinToString("") { String.format("%02X", it) }
        val asciiString = String(buffer, 0, length, Charsets.US_ASCII)
        
        terminalLogs = "UHF Stream raw (HEX): $hexString\nASCII: ${asciiString.trim()}\n" + terminalLogs.take(500)

        // 1. Check for J4212U Binary Frame Pattern:
        // Header (0xBB) -> Code (0x22: response tag) -> Payload Length -> Data (including EPC)
        // Standard Inventory frame: BB 02 22 [PC:2B] [EPC:12B] [CRC:2B] 7E or similar formats
        var i = 0
        while (i < length) {
            if ((buffer[i].toInt() and 0xFF) == 0xBB) {
                // Possible J4212U frame start
                // We need at least Header(1), Type(1), Cmd(1), Len(2), RSSI/PC(3), EPC(12), Checksum(1), End(1) = 22 bytes
                if (i + 20 < length) {
                    val cmd = buffer[i + 2].toInt() and 0xFF
                    if (cmd == 0x22) { // RFID Inventory tag scan command
                        // EPC is located after PC (typically at offset 6, length 12 bytes)
                        val epcBytes = ByteArray(12)
                        System.arraycopy(buffer, i + 6, epcBytes, 0, 12)
                        val detectedEpc = epcBytes.joinToString("") { String.format("%02X", it) }
                        
                        terminalLogs = ">>> TAG J4212U DETECTADA (FRAME BB): $detectedEpc\n" + terminalLogs
                        _scannedTags.emit(detectedEpc)
                        i += 21
                        continue
                    }
                }
            }
            i++
        }

        // 2. ASCII HEX scan fallback (ESP32 transparent bridge sending pure newline-delimited EPC tags):
        // Accumulate received characters to detect valid 24-char (12 bytes) hex tags
        for (j in 0 until length) {
            val charVal = asciiString.getOrNull(j)
            if (charVal != null && (charVal.isLetterOrDigit() || charVal == '\r' || charVal == '\n')) {
                if (charVal == '\r' || charVal == '\n') {
                    val potentialTag = hexAccumulator.toString().trim().uppercase()
                    if (potentialTag.length == 24 && potentialTag.matches(Regex("[0-9A-F]{24}"))) {
                        terminalLogs = ">>> TAG J4212U DETECTADA (ASCII): $potentialTag\n" + terminalLogs
                        _scannedTags.emit(potentialTag)
                    }
                    hexAccumulator.clear()
                } else {
                    hexAccumulator.append(charVal)
                }
            }
        }
    }

    /**
     * Manually trigger simulation read
     * Emulates reading a J4212U tag inside a local ESP32 controller
     * Generates standard Nelore / Suffolk livestock UHF RFID patterns
     */
    fun simulateRfidScan() {
        scope.launch {
            terminalLogs = "Enviando comando para simular leitura de tag no leitor J4212U...\n" + terminalLogs
            delay(400)
            
            // Simulates standard UHF RFID structures: 24 hex decimal formats (96 bits)
            // Example livestock prefixes: E2801130 for beef cattle/sheep chips.
            val standardPrefixes = listOf("E2801130", "E2003411", "E2000019")
            val randomBody = (10000000..99999999).random()
            val checksum = (10000000..99999999).random()
            val simulatedTag = "${standardPrefixes.random()}$randomBody$checksum"
            
            terminalLogs = ">>> SIMULADOR UHF: Tag J4212U Capturada: $simulatedTag\n" + terminalLogs
            _scannedTags.emit(simulatedTag)
        }
    }

    /**
     * Terminate connection
     */
    fun disconnect() {
        terminalLogs = "Desconectado do leitor RFID.\n" + terminalLogs
        isConnected = false
        connectedDeviceName = null
        closeSocket()
        connectionJob?.cancel()
        connectionJob = null
    }

    private fun closeSocket() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket", e)
        }
        socket = null
    }
}
