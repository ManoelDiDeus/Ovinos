package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HerdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HerdRepository
    val rfidManager = BluetoothRfidManager(application)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HerdRepository(database.livestockDao())
        // Seed database if empty so that the emulator immediately has realistic content to play with
        seedDatabaseIfEmpty()
    }

    // Reactive database streams
    val allAnimals: StateFlow<List<Animal>> = repository.allAnimals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWeights: StateFlow<List<AnimPeso>> = repository.allWeights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOccurrences: StateFlow<List<AnimOcor>> = repository.allOccurrences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocals: StateFlow<List<Local>> = repository.allLocals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOperators: StateFlow<List<Operador>> = repository.allOperators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- OPERATOR LOGIN SESSION ---
    var currentOperator by mutableStateOf<Operador?>(value = null)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    fun authenticateOperator(code: String, pass: String): Boolean {
        if (code.isBlank() || pass.isBlank()) {
            loginError = "Por favor, digite o código e a senha."
            return false
        }
        var success = false
        // Fetch operator on main thread because of local storage check
        viewModelScope.launch {
            val op = repository.getOperatorById(code)
            if (op != null && op.oper_senha == pass) {
                currentOperator = op
                loginError = null
                success = true
            } else {
                loginError = "Código do operador ou senha incorreta."
            }
        }
        return success
    }

    fun logoutOperator() {
        currentOperator = null
        loginError = null
    }

    fun registerOperator(operador: Operador) {
        viewModelScope.launch {
            repository.insertOperator(operador)
        }
    }

    fun deleteOperator(operCod: String) {
        viewModelScope.launch {
            repository.deleteOperator(operCod)
        }
    }


    // --- ANIMAL OPERATIONS ---
    fun registerAnimal(
        brinco: String,
        rfid: String,
        categ: String,
        datNasc: String,
        peso: Double,
        pai: String?,
        mae: String?,
        sexo: String,
        localCod: String?,
        condic: String,
        inicialCusto: Double,
        fotoPath: String? = null
    ) {
        viewModelScope.launch {
            val validBrinco = brinco.trim().uppercase()
            val validRfid = rfid.trim().uppercase()

            val animal = Animal(
                brinco = validBrinco,
                rfid = if (validRfid.isNotEmpty()) validRfid else "TID${UUID.randomUUID().toString().take(8).uppercase()}",
                categ = categ,
                dat_nasc = datNasc,
                peso = peso,
                pai = pai?.uppercase()?.takeIf { it.isNotBlank() },
                mae = mae?.uppercase()?.takeIf { it.isNotBlank() },
                foto = fotoPath,
                sexo = sexo,
                local_cod = localCod?.takeIf { it.isNotBlank() },
                condic = condic,
                anim_C_inic = inicialCusto,
                anim_custo = inicialCusto // Lifetime cumulative starts with initial cost
            )
            repository.insertAnimal(animal)

            // Save initial weight record as well
            val now = System.currentTimeMillis()
            repository.insertWeight(
                AnimPeso(
                    brinco = validBrinco,
                    data_hora = now,
                    peso = peso,
                    ipc = "3" // "bom" body score as default
                )
            )

            // Save acquisition occurrence
            repository.insertOccurrence(
                AnimOcor(
                    brinco = validBrinco,
                    data_hora = now,
                    descricao = "Registro inicial no rebanho",
                    ocor_custo = 0.0
                )
            )
        }
    }

    fun updateAnimalDetails(animal: Animal) {
        viewModelScope.launch {
            repository.updateAnimal(animal)
        }
    }

    fun deleteAnimalRecord(brinco: String) {
        viewModelScope.launch {
            repository.deleteAnimal(brinco)
        }
    }


    // --- WEIGHT OPERATIONS (BT scale interface) ---
    var scaleLiveWeight by mutableStateOf(50.0) // simulated floating live scale weight
    var scaleTerminalOutput by mutableStateOf("Conectado na balança CONSTANT via BT Terminal.\nAguardando estabilização...")
    var isScaleConnected by mutableStateOf(true)

    fun rotateScaleWeightSimulation() {
        val delta = (Math.random() - 0.5) * 1.5
        scaleLiveWeight = (scaleLiveWeight + delta).coerceIn(15.0, 950.0)
        // Simulate BT Weighing scale print string stream: e.g. "ST,GS,  65.5,kg"
        val formatted = String.format(Locale.US, "%.1f", scaleLiveWeight)
        scaleTerminalOutput = "CONSTANT BT 2.0 Terminal stream: ST,GS,  $formatted,kg\nTID UHF RFID do leitor ESP32: "
    }

    fun saveWeightRecord(brinco: String, peso: Double, ipcScore: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertWeight(
                AnimPeso(
                    brinco = brinco,
                    data_hora = now,
                    peso = peso,
                    ipc = ipcScore
                )
            )
            // Weighing event is listed under animal occurrences
            repository.insertOccurrence(
                AnimOcor(
                    brinco = brinco,
                    data_hora = now,
                    descricao = "Pesagem automatizada (GPD verificado)",
                    ocor_custo = 0.0
                )
            )
        }
    }


    // --- OCCURRENCES ---
    fun addOccurrence(brinco: String, description: String, cost: Double) {
        viewModelScope.launch {
            repository.insertOccurrence(
                AnimOcor(
                    brinco = brinco,
                    data_hora = System.currentTimeMillis(),
                    descricao = description,
                    ocor_custo = cost
                )
            )
        }
    }

    fun getWeightsForAnimal(brinco: String): Flow<List<AnimPeso>> {
        return repository.getWeightsForAnimal(brinco)
    }

    fun getOccurrencesForAnimal(brinco: String): Flow<List<AnimOcor>> {
        return repository.getOccurrencesForAnimal(brinco)
    }


    // --- LOCAL / PADDOCK MANAGEMENT ---
    fun registerLocal(code: String, description: String, area: Double, baseHeadquarters: String, fixCost: Double, varCost: Double) {
        viewModelScope.launch {
            val local = Local(
                local_cod = code.trim().uppercase(),
                descricao = description,
                local_area = area,
                local_sede = baseHeadquarters,
                local_custo_fix = fixCost,
                local_custo_var = varCost
            )
            repository.insertLocal(local)
        }
    }

    fun updateLocalRecord(local: Local) {
        viewModelScope.launch {
            repository.updateLocal(local)
        }
    }

    fun deleteLocalRecord(code: String) {
        viewModelScope.launch {
            repository.deleteLocal(code)
        }
    }

    fun executeExpensesAllocation() {
        viewModelScope.launch {
            repository.performPaddockCostAllocation()
        }
    }


    // --- BREEDING COMPATIBILITY ASSISTANT ---
    var breederSireBrinco by mutableStateOf("")
    var breederDamBrinco by mutableStateOf("")
    var compatibilityResult by mutableStateOf<BreedingCheckResult?>(null)
        private set

    fun checkPedigreeBreeding() {
        viewModelScope.launch {
            compatibilityResult = repository.verifyBreedingCompatibility(
                sireBrinco = breederSireBrinco.trim().uppercase(),
                damBrinco = breederDamBrinco.trim().uppercase()
            )
        }
    }

    fun resetBreedingChecks() {
        breederSireBrinco = ""
        breederDamBrinco = ""
        compatibilityResult = null
    }


    // --- GATE INTRO & EXIT AUTOMATION SESSION ---
    var gateSessionActive by mutableStateOf(false)
        private set
    var gateSourcePaddock by mutableStateOf<String?>(null)
        private set
    var gateDestPaddock by mutableStateOf<String?>(null)
        private set
    var sessionScannedAnimals = mutableStateListOf<Animal>()
        private set
    var sessionMissingAnimals = mutableStateListOf<Animal>()
        private set

    fun startGateSession(sourcePaddock: String?, targetPaddock: String?) {
        gateSourcePaddock = sourcePaddock
        gateDestPaddock = targetPaddock
        sessionScannedAnimals.clear()
        sessionMissingAnimals.clear()
        gateSessionActive = true

        // Initial list of "Missing" is cataloged as all active animals inside the source paddock
        viewModelScope.launch {
            val all = allAnimals.first()
            val inSource = all.filter { it.local_cod == sourcePaddock && it.condic == "ativo" }
            sessionMissingAnimals.addAll(inSource)
        }
    }

    fun simulateRfidScan(rfidCode: String) {
        if (!gateSessionActive) return
        viewModelScope.launch {
            val animal = repository.getAnimalByRfid(rfidCode) ?: repository.getAnimalByBrinco(rfidCode)
            if (animal != null && animal.condic == "ativo") {
                if (!sessionScannedAnimals.any { it.brinco == animal.brinco }) {
                    sessionScannedAnimals.add(animal)
                    sessionMissingAnimals.removeAll { it.brinco == animal.brinco }
                }
            }
        }
    }

    fun completeGateSession() {
        viewModelScope.launch {
            // Transfers all scanned animals to the target paddock/local inside the database
            val target = gateDestPaddock
            for (anim in sessionScannedAnimals) {
                repository.updateAnimalPaddock(anim.brinco, target)
                // Register passage occurrence
                repository.insertOccurrence(
                    AnimOcor(
                        brinco = anim.brinco,
                        data_hora = System.currentTimeMillis(),
                        descricao = "Passagem pelo curral de ${gateSourcePaddock ?: "pasto livre"} para ${target ?: "curral"}",
                        ocor_custo = 0.0
                    )
                )
            }
            gateSessionActive = false
            gateSourcePaddock = null
            gateDestPaddock = null
            sessionScannedAnimals.clear()
            sessionMissingAnimals.clear()
        }
    }

    fun cancelGateSession() {
        gateSessionActive = false
        gateSourcePaddock = null
        gateDestPaddock = null
        sessionScannedAnimals.clear()
        sessionMissingAnimals.clear()
    }


    // --- INITIAL SEED METHOD ---
    private fun seedDatabaseIfEmpty() {
        viewModelScope.launch {
            // Always ensure the 'adm' operator is present
            repository.insertOperator(Operador("adm", "administrador", "1", "admin"))

            val count = repository.allOperators.first().size
            if (count <= 1) {
                // Seed 2 operators
                repository.insertOperator(Operador("001", "Roberto Silva", "A", "123456"))
                repository.insertOperator(Operador("002", "Manoel Castro", "O", "888888"))

                // Seed 3 Locals (Paddocks)
                repository.insertLocal(Local("P01", "Piquete Norte - Tifton", 12.50, "Sede Principal", 150.00, 45.00))
                repository.insertLocal(Local("P02", "Rotativo Sul - Brachiaria", 8.20, "Sede Auxiliar", 120.00, 30.00))
                repository.insertLocal(Local("P03", "Capineira Maternidade", 3.10, "Sede Principal", 80.00, 15.00))

                // Seed 6 Animals
                // Category options: borrego, cordeiro, matriz, reprodutor, castrado, descarte
                repository.insertAnimal(
                    Animal("RM101", "96TID7766551", "reprodutor", "2023-01-15", 85.0, null, null, null, "M", "P01", "ativo", 2500.00, 2500.00)
                )
                repository.insertAnimal(
                    Animal("RM102", "96TID7766552", "matriz", "2023-03-22", 68.4, null, null, null, "F", "P01", "ativo", 1200.00, 1200.00)
                )
                repository.insertAnimal(
                    Animal("RM103", "96TID7766553", "matriz", "2024-02-10", 62.0, "RM101", "RM102", null, "F", "P01", "ativo", 950.00, 950.00)
                )
                repository.insertAnimal(
                    Animal("RM104", "96TID7766554", "cordeiro", "2025-05-30", 24.5, "RM101", "RM103", null, "M", "P02", "ativo", 450.00, 450.00) // consanguinity alert! (RM101 is parent of RM103, and RM101/RM103 bred RM104)
                )
                repository.insertAnimal(
                    Animal("RM105", "96TID7766555", "borrego", "2025-08-12", 18.2, "RM101", "RM102", null, "F", "P02", "ativo", 300.00, 300.00)
                )
                repository.insertAnimal(
                    Animal("RM106", "96TID7766556", "descarte", "2021-11-04", 75.0, null, null, null, "M", "P03", "ativo", 600.00, 600.00)
                )

                // Seed some weight records
                val now = System.currentTimeMillis()
                val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000
                val twoMonthsAgo = now - 60L * 24 * 60 * 60 * 1000

                repository.insertWeight(AnimPeso(0, "RM104", twoMonthsAgo, 15.0, "3"))
                repository.insertWeight(AnimPeso(0, "RM104", oneMonthAgo, 20.2, "3"))
                repository.insertWeight(AnimPeso(0, "RM104", now, 24.5, "3"))

                repository.insertWeight(AnimPeso(0, "RM101", oneMonthAgo, 82.0, "4"))
                repository.insertWeight(AnimPeso(0, "RM101", now, 85.0, "4"))

                // Seed occurrences
                repository.insertOccurrence(AnimOcor(0, "RM101", twoMonthsAgo, "Vacinação contra Febre Aftosa", 15.0))
                repository.insertOccurrence(AnimOcor(0, "RM101", oneMonthAgo, "Vermifugação semestral", 12.5))
                repository.insertOccurrence(AnimOcor(0, "RM102", oneMonthAgo, "Vermifugação semestral", 12.5))
                repository.insertOccurrence(AnimOcor(0, "RM104", oneMonthAgo, "Suplementação mineral inicial", 8.00))
            }
        }
    }
}

// Simple Helper stateful list wrapper for Compose view state references
fun <T> mutableStateListOf() = androidx.compose.runtime.mutableStateListOf<T>()
