package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import coil.compose.AsyncImage
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Primary Color Palette
val FarmGreen = Color(0xFF2E6F40)
val DarkGreen = Color(0xFF143F20)
val SoftGold = Color(0xFFE5A93B)
val LightSage = Color(0xFFF0F5F1)
val CustomSlate = Color(0xFF323B36)
val OrangeAlert = Color(0xFFD35400)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationWrapper(viewModel: HerdViewModel) {
    val currentOperator = viewModel.currentOperator

    if (currentOperator == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        MainAppLayout(viewModel = viewModel)
    }
}

// --- SUB-SCREEN: 1. LOGIN ---
@Composable
fun LoginScreen(viewModel: HerdViewModel) {
    var operCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val operators by viewModel.allOperators.collectAsStateWithLifecycle(emptyList())
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGreen, FarmGreen)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Visual Cow/Sheep Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, SoftGold.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "Farm Logo",
                    tint = SoftGold,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CURRAL GESTÃO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
            Text(
                text = "Sistema Integrado de Controle de Rebanho",
                fontSize = 14.sp,
                color = LightSage.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Identificação do Operador",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CustomSlate,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = operCode,
                        onValueChange = { operCode = it },
                        label = { Text("Código do Operador (ex: 001)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FarmGreen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen,
                            focusedLabelColor = FarmGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("operator_code_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha (ex: 123456)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FarmGreen) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen,
                            focusedLabelColor = FarmGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("operator_password_input")
                    )

                    viewModel.loginError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val ok = viewModel.authenticateOperator(operCode, password)
                            if (ok) {
                                Toast.makeText(context, "Sessão iniciada!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button")
                    ) {
                        Text("ENTRAR NO SISTEMA", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Lista de Perfis Cadastrados (Acesso Rápido):",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(operators) { op ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        operCode = op.oper_cod
                                        password = op.oper_senha
                                    }
                                    .background(LightSage)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (op.oper_niv == "A") Icons.Default.AdminPanelSettings else Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = FarmGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(op.oper_nome, fontSize = 13.sp, color = CustomSlate, fontWeight = FontWeight.Medium)
                                }
                                Text("Cód: ${op.oper_cod}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- MAIN TAB NAVIGATION CONTAINER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: HerdViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val listTabs = listOf(
        TabItem("Início", Icons.Default.Dashboard, 0),
        TabItem("Rebanho", Icons.Default.List, 1),
        TabItem("Pesagem", Icons.Default.Scale, 2),
        TabItem("Curral", Icons.Default.CompareArrows, 3),
        TabItem("Genética", Icons.Default.FamilyRestroom, 4),
        TabItem("Config / Custos", Icons.Default.Settings, 5)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = SoftGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CurralGestão",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = viewModel.currentOperator?.oper_nome ?: "",
                            fontSize = 12.sp,
                            color = LightSage,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (viewModel.currentOperator?.oper_niv == "A") "Administrador" else "Operador",
                            fontSize = 11.sp,
                            color = SoftGold
                        )
                    }
                    IconButton(onClick = { viewModel.logoutOperator() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FarmGreen)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                listTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.index,
                        onClick = { selectedTab = tab.index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FarmGreen,
                            selectedTextColor = FarmGreen,
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray,
                            indicatorColor = LightSage
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { selectedTab = it })
                1 -> HerdCattleViewScreen(viewModel = viewModel)
                2 -> ScaleWeightTerminalScreen(viewModel = viewModel)
                3 -> RFIDGateControllerScreen(viewModel = viewModel)
                4 -> PedigreeBreedingScreen(viewModel = viewModel)
                5 -> ConfigAndCostsScreen(viewModel = viewModel)
            }
        }
    }
}

data class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val index: Int)


// --- SUB-SCREEN: 0. DASHBOARD ---
@Composable
fun DashboardScreen(viewModel: HerdViewModel, onNavigateToTab: (Int) -> Unit) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val locals by viewModel.allLocals.collectAsStateWithLifecycle()
    val weights by viewModel.allWeights.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FarmGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Bem-vindo de volta, ${viewModel.currentOperator?.oper_nome}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "O rebanho está distribuído em ${locals.size} piquetes ativos. Use as seções de pesagem rápida por Bluetooth ou scan de curral via RFID para atualizar seus dados localmente.",
                        fontSize = 13.sp,
                        color = LightSage.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Live Dynamic Statistics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active animals count card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Animais Ativos", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = animals.filter { it.condic == "ativo" }.size.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FarmGreen
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = LightSage)
                        Text(
                            text = "Total Geral: ${animals.size}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Cumulative value or total average weight
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    val activeAnimals = animals.filter { it.condic == "ativo" }
                    val avgWeight = if (activeAnimals.isNotEmpty()) activeAnimals.map { it.peso }.average() else 0.0
                    val totalInvestment = activeAnimals.sumOf { it.anim_custo }

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Peso Médio", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f kg", avgWeight),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CustomSlate
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = LightSage)
                        Text(
                            text = String.format(Locale.getDefault(), "Custo Total: R$ %.2f", totalInvestment),
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Action Quick Access Buttons
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ações Rápidas de Campo", fontWeight = FontWeight.Bold, color = CustomSlate, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionItem(
                            title = "Nova Pesagem",
                            icon = Icons.Default.Scale,
                            onClick = { onNavigateToTab(2) }
                        )
                        QuickActionItem(
                            title = "Scan Curral",
                            icon = Icons.Default.CompareArrows,
                            onClick = { onNavigateToTab(3) }
                        )
                        QuickActionItem(
                            title = "Checar Parentes",
                            icon = Icons.Default.Psychology,
                            onClick = { onNavigateToTab(4) }
                        )
                        QuickActionItem(
                            title = "Ratear Custos",
                            icon = Icons.Default.Paid,
                            onClick = { onNavigateToTab(5) }
                        )
                    }
                }
            }
        }

        // Paddock grazing load display
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lotação por Piquetes", fontWeight = FontWeight.Bold, color = CustomSlate, fontSize = 15.sp)
                        Text("${locals.size} Piquetes", fontSize = 12.sp, color = FarmGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (locals.isEmpty()) {
                        Text("Nenhum piquete cadastrado.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        locals.forEach { loc ->
                            val headcount = animals.filter { it.local_cod == loc.local_cod && it.condic == "ativo" }.size
                            val loadRate = headcount / loc.local_area

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(loc.descricao, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CustomSlate)
                                    Text("$headcount cab | Lotação: ${String.format(Locale.getDefault(), "%.1f", loadRate)} áv/ha", fontSize = 12.sp, color = Color.Gray)
                                }
                                LinearProgressIndicator(
                                    progress = (headcount / 10f).coerceIn(0f, 1f),
                                    color = if (loadRate > 3.0) OrangeAlert else FarmGreen,
                                    trackColor = LightSage,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Seed disclaimer & information about BT scale & ESP32 RFID Reader
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightSage),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, FarmGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CellTower, contentDescription = null, tint = FarmGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hardware Integrado", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkGreen)
                        Text(
                            "Leitor RFID UHF ESP32 (Model J4212U) e balança de gado CONSTANT Bluetooth ativos e simulados localmente neste terminal de gerenciamento.",
                            fontSize = 11.sp,
                            color = CustomSlate.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(LightSage, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = FarmGreen)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CustomSlate)
    }
}


// --- SUB-SCREEN: 1. HERD MANAGEMENT (LIST / EDIT / REGISTER) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerdCattleViewScreen(viewModel: HerdViewModel) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val locals by viewModel.allLocals.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("Todos") }
    var conditionFilter by remember { mutableStateOf("ativo") }

    var selectedAnimalForDetail by remember { mutableStateOf<Animal?>(null) }
    var isRegisteringNewAnimal by remember { mutableStateOf(false) }

    val filteredAnimals = animals.filter {
        val matchesSearch = it.brinco.contains(searchQuery, true) || it.rfid.contains(searchQuery, true)
        val matchesCategory = categoryFilter == "Todos" || it.categ.equals(categoryFilter, true)
        val matchesCondition = conditionFilter == "Todos" || it.condic.equals(conditionFilter, true)
        matchesSearch && matchesCategory && matchesCondition
    }

    Scaffold(
        floatingActionButton = {
            if (!isRegisteringNewAnimal && selectedAnimalForDetail == null) {
                FloatingActionButton(
                    onClick = { isRegisteringNewAnimal = true },
                    containerColor = FarmGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_animal_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Animal")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightSage)
        ) {
            if (isRegisteringNewAnimal) {
                RegisterAnimalForm(
                    locals = locals,
                    viewModel = viewModel,
                    onSave = { brinco, rfid, categ, datNasc, peso, pai, mae, sexo, local, cond, inicialCost, fotoPath ->
                        viewModel.registerAnimal(
                            brinco, rfid, categ, datNasc, peso, pai, mae, sexo, local, cond, inicialCost, fotoPath
                        )
                        isRegisteringNewAnimal = false
                    },
                    onCancel = { isRegisteringNewAnimal = false }
                )
            } else if (selectedAnimalForDetail != null) {
                AnimalDetailPane(
                    animal = selectedAnimalForDetail!!,
                    locals = locals,
                    viewModel = viewModel,
                    onBack = { selectedAnimalForDetail = null }
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gerenciamento de Rebanho",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Pesquisar por brinco ou RFID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .testTag("cattle_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FarmGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filters: Catgories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Todos", "matriz", "reprodutor", "cordeiro", "borrego", "descarte")
                        var expandedCatBy by remember { mutableStateOf(false) }

                        // Category Dropdown Filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedCatBy = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cat: $categoryFilter", fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = expandedCatBy,
                                onDismissRequest = { expandedCatBy = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.uppercase()) },
                                        onClick = {
                                            categoryFilter = cat
                                            expandedCatBy = false
                                        }
                                    )
                                }
                            }
                        }

                        // Condition Dropdown Filter (ativo, vendido, morto)
                        val conditions = listOf("Todos", "ativo", "inativo", "abatido", "morto", "vendido")
                        var expandedCond by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedCond = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cond: $conditionFilter", fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = expandedCond,
                                onDismissRequest = { expandedCond = false }
                            ) {
                                conditions.forEach { cond ->
                                    DropdownMenuItem(
                                        text = { Text(cond.uppercase()) },
                                        onClick = {
                                            conditionFilter = cond
                                            expandedCond = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredAnimals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Nenhum animal correspondente encontrado.", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredAnimals) { anim ->
                                AnimalItemCard(
                                    animal = anim,
                                    onClick = { selectedAnimalForDetail = anim }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalItemCard(animal: Animal, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("animal_card_${animal.brinco}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cattle avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightSage),
                contentAlignment = Alignment.Center
            ) {
                if (!animal.foto.isNullOrEmpty()) {
                    AsyncImage(
                        model = animal.foto,
                        contentDescription = "Foto do animal",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = if (animal.sexo == "M") "M" else "F",
                        color = if (animal.sexo == "M") Color(0xFF1976D2) else Color(0xFFC2185B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "BRINCO: ${animal.brinco}",
                        fontWeight = FontWeight.Bold,
                        color = CustomSlate,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (animal.condic) {
                                    "ativo" -> FarmGreen.copy(alpha = 0.15f)
                                    "vendido" -> Color.LightGray
                                    "morto" -> OrangeAlert.copy(alpha = 0.15f)
                                    else -> Color.DarkGray.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = animal.condic.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (animal.condic) {
                                "ativo" -> FarmGreen
                                "vendido" -> Color.DarkGray
                                "morto" -> OrangeAlert
                                else -> CustomSlate
                            }
                        )
                    }
                }
                Text(
                    text = "RFID: ${animal.rfid}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${animal.categ.uppercase()} | ${animal.peso} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CustomSlate
                    )
                    Text(
                        text = "Piquete: ${animal.local_cod ?: "Curral"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmGreen
                    )
                }
            }
        }
    }
}


// --- FORM: REGISTER NEW ANIMAL ---
fun saveBitmapToCache(context: android.content.Context, bitmap: Bitmap): String? {
    return try {
        val file = File(context.cacheDir, "animal_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun RegisterAnimalForm(
    locals: List<Local>,
    viewModel: HerdViewModel,
    onSave: (String, String, String, String, Double, String?, String?, String, String?, String, Double, String?) -> Unit,
    onCancel: () -> Unit
) {
    var brinco by remember { mutableStateOf("") }
    var rfid by remember { mutableStateOf("") }
    var categ by remember { mutableStateOf("matriz") }
    var datNasc by remember { mutableStateOf("2025-01-01") }
    var peso by remember { mutableStateOf("") }
    var pai by remember { mutableStateOf("") }
    var mae by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("F") }
    var localCod by remember { mutableStateOf("") }
    var condic by remember { mutableStateOf("ativo") }
    var custoInicial by remember { mutableStateOf("") }
    
    // Camera and Photo states
    val context = LocalContext.current
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var isRfidScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // RFID Bluetooth Integration Managers
    val rfidManager = viewModel.rfidManager
    var showDeviceMenu by remember { mutableStateOf(false) }

    // Automatic collection of scanned tag signals directly inside Form's State
    LaunchedEffect(rfidManager) {
        rfidManager.scannedTags.collect { tag ->
            rfid = tag
            Toast.makeText(context, "RFID lido automaticamente: $tag", Toast.LENGTH_LONG).show()
        }
    }

    // Activity launcher for capturing visual preview from camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val path = saveBitmapToCache(context, bitmap)
            if (path != null) {
                capturedPhotoPath = path
                Toast.makeText(context, "Foto capturada pela câmera!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Captura de foto cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    val presetImages = listOf(
        Pair("Vaca Nelore", "https://images.unsplash.com/photo-1546445317-29f4545e6d51?w=400"),
        Pair("Ovelha Suffolk", "https://images.unsplash.com/photo-1516467508483-a7212febe31a?w=400"),
        Pair("Cordeiro Elite", "https://images.unsplash.com/photo-1484557985045-edd9b3be4816?w=400")
    )

    val categories = listOf("borrego", "cordeiro", "matriz", "reprodutor", "castrado", "descarte")
    val conditions = listOf("ativo", "inativo", "abatido", "morto", "vendido")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Novo Cadastro de Animal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Text(
                        text = "Gerenciamento inteligente de identificação",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // --- SEÇÃO 1: FOTO DO ANIMAL (CÂMERA & PRESETS) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Foto do Animal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkGreen
                    )
                    Text(
                        text = "Capture a foto real pela câmera ou utilize fotos demonstrativas de lote.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    if (capturedPhotoPath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightSage),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = capturedPhotoPath,
                                contentDescription = "Foto do animal",
                                modifier = Modifier.fillMaxSize()
                            )
                            // Delete Overlay Button
                            IconButton(
                                onClick = { capturedPhotoPath = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.White)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .background(FarmGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Foto Ativa", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Empty states design
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .background(LightSage.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Sem foto associada", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tirar Foto", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // Rotate presets on click
                                val nextPreset = presetImages.random()
                                capturedPhotoPath = nextPreset.second
                                Toast.makeText(context, "Carregada foto de lote: ${nextPreset.first}", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, FarmGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulador Foto", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- SEÇÃO 2: DADOS DE IDENTIFICAÇÃO & RFID INTEGRADO ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Bluetooth RFID UHF J4212U",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkGreen
                    )
                    
                    // Live Status Light Banner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    rfidManager.isConnected -> Color(0xFFE8F5E9)
                                    rfidManager.isConnecting -> Color(0xFFFFFDE7)
                                    else -> Color(0xFFECEFF1)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                when {
                                    rfidManager.isConnected -> Color(0xFF81C784)
                                    rfidManager.isConnecting -> Color(0xFFFFF176)
                                    else -> Color(0xFFCFD8DC)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    when {
                                        rfidManager.isConnected -> Color(0xFF2E7D32)
                                        rfidManager.isConnecting -> Color(0xFFFBC02D)
                                        else -> Color(0xFF78909C)
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    rfidManager.isConnected -> "Conectado ao Leitor: ${rfidManager.connectedDeviceName}"
                                    rfidManager.isConnecting -> "Estabelecendo fluxo bluetooth (SPP)..."
                                    else -> "Leitor Desconectado"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                            Text(
                                text = "Padrão UHF RFID Short Range Model J4212U",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        if (rfidManager.isConnected) {
                            TextButton(onClick = { rfidManager.disconnect() }) {
                                Text("Desconectar", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Pair/Connection controls
                    if (!rfidManager.isConnected && !rfidManager.isConnecting) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Button(
                                        onClick = { 
                                            rfidManager.updatePairedDevices()
                                            showDeviceMenu = true 
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LightSage, contentColor = DarkGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Selecionar Dispositivo (${rfidManager.pairedDevices.size})", fontSize = 11.sp)
                                    }

                                    DropdownMenu(
                                        expanded = showDeviceMenu,
                                        onDismissRequest = { showDeviceMenu = false }
                                    ) {
                                        if (rfidManager.pairedDevices.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("Nenhum pareado. Toque para recarregar") },
                                                onClick = {
                                                    rfidManager.updatePairedDevices()
                                                    showDeviceMenu = false
                                                }
                                            )
                                        } else {
                                            rfidManager.pairedDevices.forEach { dev ->
                                                val name = dev.name ?: "Equipamento Sem Nome"
                                                val addr = dev.address
                                                DropdownMenuItem(
                                                    text = { Text("$name\n($addr)", fontSize = 12.sp) },
                                                    onClick = {
                                                        rfidManager.connect(addr)
                                                        showDeviceMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { rfidManager.updatePairedDevices() },
                                    modifier = Modifier.background(LightSage, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Recarregar", tint = FarmGreen)
                                }
                            }
                        }
                    }

                    // Live hardware CLI/Terminal Logging Console
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Consola Serial J4212U (ESP32 Raw)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF81C784).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("A0 22 ACTIVE", color = Color(0xFF2E7D32), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(94.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = rfidManager.terminalLogs,
                                color = Color(0xFF4CAF50),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Input visual forms
                    OutlinedTextField(
                        value = brinco,
                        onValueChange = { if (it.length <= 5) brinco = it.uppercase() },
                        label = { Text("Brinco identificador (Máx 5 letras/números)*") },
                        supportingText = { Text("Opcional se RFID for o ID principal") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FarmGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_brinco")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rfid,
                            onValueChange = { if (it.length <= 24) rfid = it.uppercase() },
                            label = { Text("Código RFID TID/EPC *") },
                            placeholder = { Text("Aguardando leitura de tag...") },
                            supportingText = { Text("EPC UHF J4212U de 24 hex-chars") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FarmGreen)
                        )

                        // Action UHF Reading buttons
                        Button(
                            onClick = {
                                rfidManager.simulateRfidScan()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = CustomSlate),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = "Active scanning")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ler UHF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SEÇÃO 3: CARACTERÍSTICAS E PARENTESCO ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Características Biológicas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkGreen
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Sex Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sexo*", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = sexo == "F", onClick = { sexo = "F" })
                                Text("Fêmea", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                RadioButton(selected = sexo == "M", onClick = { sexo = "M" })
                                Text("Macho", fontSize = 12.sp)
                            }
                        }

                        // Categoria dropdown
                        var expCat by remember { mutableStateOf(false) }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Categoria*", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                            Box {
                                OutlinedButton(
                                    onClick = { expCat = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(categ.uppercase(), fontSize = 12.sp)
                                }
                                DropdownMenu(expanded = expCat, onDismissRequest = { expCat = false }) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(text = { Text(cat.uppercase()) }, onClick = {
                                            categ = cat
                                            expCat = false
                                        })
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = peso,
                            onValueChange = { peso = it },
                            label = { Text("Peso Inicial (kg)*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = datNasc,
                            onValueChange = { datNasc = it },
                            label = { Text("Data Nasc. (yyyy-MM-dd)") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = pai,
                            onValueChange = { if (it.length <= 5) pai = it.uppercase() },
                            label = { Text("Pai (Brinco)") },
                            placeholder = { Text("Opcional") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = mae,
                            onValueChange = { if (it.length <= 5) mae = it.uppercase() },
                            label = { Text("Mãe (Brinco)") },
                            placeholder = { Text("Opcional") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- SEÇÃO 4: CONFIGURAÇÕES E CUSTOS ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Configuração de Manejo Inicial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkGreen
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        var expLoc by remember { mutableStateOf(false) }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Piquete inicial", fontSize = 13.sp, color = Color.Gray)
                            Box {
                                OutlinedButton(
                                    onClick = { expLoc = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(localCod.takeIf { it.isNotBlank() } ?: "CURRAL", fontSize = 12.sp)
                                }
                                DropdownMenu(expanded = expLoc, onDismissRequest = { expLoc = false }) {
                                    DropdownMenuItem(text = { Text("CURRAL (Sem local)") }, onClick = {
                                        localCod = ""
                                        expLoc = false
                                    })
                                    locals.forEach { loc ->
                                        DropdownMenuItem(text = { Text("${loc.local_cod} - ${loc.descricao}") }, onClick = {
                                            localCod = loc.local_cod
                                            expLoc = false
                                        })
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = custoInicial,
                            onValueChange = { custoInicial = it },
                            label = { Text("Valor de Aquisição (R$)*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }
        }

        // --- SEÇÃO 5: SUBMIT ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val trimmedBrinco = brinco.trim().uppercase()
                        val trimmedRfid = rfid.trim().uppercase()
                        if (trimmedBrinco.isBlank()) {
                            Toast.makeText(context, "Erro: Identificador Brinco é obrigatório!", Toast.LENGTH_LONG).show()
                        } else if (trimmedRfid.isBlank()) {
                            Toast.makeText(context, "Erro: Código RFID UHF é obrigatório para integração!", Toast.LENGTH_LONG).show()
                        } else if (peso.toDoubleOrNull() == null) {
                            Toast.makeText(context, "Erro: Digite um Peso Inicial numérico válido!", Toast.LENGTH_LONG).show()
                        } else if (custoInicial.toDoubleOrNull() == null) {
                            Toast.makeText(context, "Erro: Digite um Valor de Aquisição numérico válido!", Toast.LENGTH_LONG).show()
                        } else {
                            onSave(
                                trimmedBrinco,
                                trimmedRfid,
                                categ,
                                datNasc,
                                peso.toDoubleOrNull() ?: 0.0,
                                pai.trim(),
                                mae.trim(),
                                sexo,
                                localCod.trim(),
                                condic,
                                custoInicial.toDoubleOrNull() ?: 0.0,
                                capturedPhotoPath
                            )
                            Toast.makeText(context, "Cadastro de $trimmedBrinco realizado com sucesso!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_animal_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SALVAR E REGISTRAR ANIMAL", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onCancel,
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CustomSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


// --- DETAIL PANE: ANIMAL VIEW ---
@Composable
fun AnimalDetailPane(
    animal: Animal,
    locals: List<Local>,
    viewModel: HerdViewModel,
    onBack: () -> Unit
) {
    val weightsState = viewModel.getWeightsForAnimal(animal.brinco).collectAsStateWithLifecycle(emptyList<AnimPeso>())
    val weights = weightsState.value
    val occurrencesState = viewModel.getOccurrencesForAnimal(animal.brinco).collectAsStateWithLifecycle(emptyList<AnimOcor>())
    val occurrences = occurrencesState.value

    var isAddingOcor by remember { mutableStateOf(false) }
    var ocorDesc by remember { mutableStateOf("") }
    var ocorCost by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header inside sheet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Voltar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dossiê: Animal ${animal.brinco}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }
        }

        // Optional Anim Image Banner
        if (!animal.foto.isNullOrEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = animal.foto,
                        contentDescription = "Foto de ${animal.brinco}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Animal main stats card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BRINCO ${animal.brinco}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustomSlate
                        )
                        Box(
                            modifier = Modifier
                                .background(FarmGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = animal.categ.uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text("Peso Atual", fontSize = 12.sp, color = Color.Gray)
                            Text("${animal.peso} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                        }
                        Column {
                            Text("Local Atual", fontSize = 12.sp, color = Color.Gray)
                            Text(animal.local_cod ?: "Curral Central", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FarmGreen)
                        }
                        Column {
                            Text("Custo Acumulado", fontSize = 12.sp, color = Color.Gray)
                            Text(String.format(Locale.getDefault(), "R$ %.2f", animal.anim_custo), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OrangeAlert)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = LightSage)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pedigree view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pai (Reprodutor)", fontSize = 12.sp, color = Color.Gray)
                            Text(animal.pai ?: "Indefinido", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CustomSlate)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mãe (Matriz)", fontSize = 12.sp, color = Color.Gray)
                            Text(animal.mae ?: "Indefinida", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CustomSlate)
                        }
                    }
                }
            }
        }

        // Histórico de Pesagem (Weights curves)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Curva de Ganhos de Peso (Historico)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CustomSlate)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (weights.isEmpty()) {
                        Text("Sem registros de peso anteriores.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        // Inline canvas plot
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(LightSage, RoundedCornerShape(8.dp))
                        ) {
                            val maxW = weights.map { it.peso }.maxOrNull() ?: 100.0
                            val minW = weights.map { it.peso }.minOrNull() ?: 0.0
                            val range = (maxW - minW).coerceAtLeast(1.0)

                            val pointsCount = weights.size
                            if (pointsCount > 1) {
                                val path = Path()
                                val stepX = size.width / (pointsCount - 1)

                                weights.reversed().forEachIndexed { idx, p ->
                                    val x = idx * stepX
                                    val y = size.height - ((p.peso - minW) / range * (size.height - 40f) + 20f)
                                    if (idx == 0) {
                                        path.moveTo(x, y.toFloat())
                                    } else {
                                        path.lineTo(x, y.toFloat())
                                    }
                                    drawCircle(
                                        color = FarmGreen,
                                        radius = 6f,
                                        center = Offset(x, y.toFloat())
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = FarmGreen,
                                    style = Stroke(width = 4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weights list
                        weights.forEach { w ->
                            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(w.data_hora))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                                Row {
                                    Text("${w.peso} kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("IPC: ${w.ipc}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FarmGreen)
                                }
                            }
                            HorizontalDivider(color = LightSage)
                        }
                    }
                }
            }
        }

        // ocorrências list
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ocorrências na Vida (Saúde / Nutrição)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CustomSlate)
                        if (!isAddingOcor) {
                            TextButton(onClick = { isAddingOcor = true }) {
                                Text("+ Registrar", color = FarmGreen, fontSize = 13.sp)
                            }
                        }
                    }

                    if (isAddingOcor) {
                        Column(
                            modifier = Modifier
                                .background(LightSage, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            OutlinedTextField(
                                value = ocorDesc,
                                onValueChange = { ocorDesc = it },
                                label = { Text("Descrição (medicamento, brinco quebrado, cria)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = ocorCost,
                                onValueChange = { ocorCost = it },
                                label = { Text("Custo Extra Evento (R$) (Opcional)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { isAddingOcor = false }) { Text("Cancelar") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (ocorDesc.isNotBlank()) {
                                            val costVal = ocorCost.toDoubleOrNull() ?: 0.0
                                            viewModel.addOccurrence(animal.brinco, ocorDesc, costVal)
                                            ocorDesc = ""
                                            ocorCost = ""
                                            isAddingOcor = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                                ) {
                                    Text("ADICIONAR", color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (occurrences.isEmpty()) {
                        Text("Nenhuma ocorrência registrada.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        occurrences.forEach { oc ->
                            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(oc.data_hora))
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(oc.descricao, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CustomSlate)
                                    Text("R$ ${String.format(Locale.getDefault(), "%.2f", oc.ocor_custo)}", fontSize = 13.sp, color = OrangeAlert)
                                }
                                Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                HorizontalDivider(color = LightSage, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SUB-SCREEN: 2. SCALE WEIGHING TERMINAL (BLUETOOTH SIMULATION) ---
@Composable
fun ScaleWeightTerminalScreen(viewModel: HerdViewModel) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    var selectedBrinco by remember { mutableStateOf("") }
    var selectedIpc by remember { mutableStateOf("3") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Weighing simulation worker loop
    LaunchedEffect(viewModel.isScaleConnected) {
        while (viewModel.isScaleConnected) {
            viewModel.rotateScaleWeightSimulation()
            delay(1200)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Terminal Bluetooth: Pesagem & Produção",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Integração instantânea com balanças CONSTANT Gado no curral.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // Live Scale Terminal View
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CustomSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (viewModel.isScaleConnected) Color.Green else Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Balança CONSTANT Bluetooth", color = Color.White, fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = { viewModel.isScaleConnected = !viewModel.isScaleConnected },
                            colors = ButtonDefaults.textButtonColors(contentColor = SoftGold)
                        ) {
                            Text(if (viewModel.isScaleConnected) "Desconectar" else "Conectar")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Digital Weight Indicator
                    Text(
                        text = if (viewModel.isScaleConnected) {
                            String.format(Locale.US, "%.1f kg", viewModel.scaleLiveWeight)
                        } else {
                            "---.- kg"
                        },
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("digital_weight_display")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Terminal Live Feed
                    Text(
                        text = viewModel.scaleTerminalOutput,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
        }

        // Live Weight Adjuster (Simulates cattle movement/fluctuations)
        if (viewModel.isScaleConnected) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Simulador de Flutuação da Balança", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CustomSlate)
                        Text("Arrastar abaixo simula o animal se movimentando ou outra ovelha/boi subindo.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = viewModel.scaleLiveWeight.toFloat(),
                            onValueChange = { viewModel.scaleLiveWeight = it.toDouble() },
                            valueRange = 10f..400f,
                            colors = SliderDefaults.colors(
                                thumbColor = FarmGreen,
                                activeTrackColor = FarmGreen
                            )
                        )
                    }
                }
            }
        }

        // Record Form (Brinco and IPC selection)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gravar Pesagem do Animal", fontWeight = FontWeight.Bold, color = CustomSlate, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Animal Quick selection
                    var isDropdownExp by remember { mutableStateOf(false) }
                    Text("Selecionar Animal*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(
                            onClick = { isDropdownExp = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (selectedBrinco.isBlank()) "Escolha um brinco..." else "Brinco: $selectedBrinco",
                                maxLines = 1
                            )
                        }
                        DropdownMenu(
                            expanded = isDropdownExp,
                            onDismissRequest = { isDropdownExp = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            animals.filter { it.condic == "ativo" }.forEach { anim ->
                                DropdownMenuItem(
                                    text = { Text("BRINCO: ${anim.brinco} | Peso ant: ${anim.peso}kg | Cat: ${anim.categ}") },
                                    onClick = {
                                        selectedBrinco = anim.brinco
                                        isDropdownExp = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // IPC Scores Selector (Body Condition Score 1-5)
                    Text("IPC (Índice de Peso Corporal - Escore de 1 a 5)*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("1: Muito Magro | 2: Magro | 3: Bom | 4: Gordo | 5: Muito Gordo", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (score in 1..5) {
                            val scoreStr = score.toString()
                            val selected = selectedIpc == scoreStr
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedIpc = scoreStr }
                                    .background(
                                        if (selected) FarmGreen else LightSage,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = scoreStr,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else CustomSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (selectedBrinco.isBlank()) {
                                Toast.makeText(context, "Por favor selecione um animal!", Toast.LENGTH_SHORT).show()
                            } else {
                                val currentWeight = viewModel.scaleLiveWeight
                                viewModel.saveWeightRecord(selectedBrinco, currentWeight, selectedIpc)
                                Toast.makeText(context, "Pesagem de $selectedBrinco gravada com sucesso!", Toast.LENGTH_SHORT).show()
                                selectedBrinco = ""
                                selectedIpc = "3"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("record_weight_button")
                    ) {
                        Text("GRAVAR E ATUALIZAR REBANHO", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


// --- SUB-SCREEN: 3. RFID GATE CONTROLLER (CORRAL GATE COUNTING) ---
@Composable
fun RFIDGateControllerScreen(viewModel: HerdViewModel) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val locals by viewModel.allLocals.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Automação Corral: Controle RFID UHF",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Automatize a contagem de ovelhas/boi que saem do curral para o pasto usando leitor ESP32. Descubra em tempo real se alguma ficou para trás ou sumiu.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        if (!viewModel.gateSessionActive) {
            // Setup gate scan session
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    var sourcePaddock by remember { mutableStateOf("") }
                    var destPaddock by remember { mutableStateOf("") }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nova Sessão de Entrada/Saída", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Source paddock dropdown
                        var expSource by remember { mutableStateOf(false) }
                        Text("Piquete de Origem*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Box {
                            OutlinedButton(onClick = { expSource = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (sourcePaddock.isBlank()) "Selecione a Origem..." else "Origem: $sourcePaddock")
                            }
                            DropdownMenu(expanded = expSource, onDismissRequest = { expSource = false }) {
                                locals.forEach { loc ->
                                    val count = animals.filter { it.local_cod == loc.local_cod && it.condic == "ativo" }.size
                                    DropdownMenuItem(
                                        text = { Text("${loc.descricao} ($count cabeças)") },
                                        onClick = {
                                            sourcePaddock = loc.local_cod
                                            expSource = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Destination paddock dropdown
                        var expDest by remember { mutableStateOf(false) }
                        Text("Piquete ou Destino de Destino*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Box {
                            OutlinedButton(onClick = { expDest = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (destPaddock.isBlank()) "Selecione o Destino..." else "Destino: $destPaddock")
                            }
                            DropdownMenu(expanded = expDest, onDismissRequest = { expDest = false }) {
                                locals.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.descricao) },
                                        onClick = {
                                            destPaddock = loc.local_cod
                                            expDest = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (sourcePaddock.isBlank() || destPaddock.isBlank() || sourcePaddock == destPaddock) {
                                    Toast.makeText(context, "Selecione piquetes diferentes como origem e destino!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.startGateSession(sourcePaddock, destPaddock)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_gate_session_button")
                        ) {
                            Text("ABRIR SESSÃO DE CONTROLE RFID", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            // Active Scanning session
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sessão Ativa: ${viewModel.gateSourcePaddock} ➔ ${viewModel.gateDestPaddock}", color = FarmGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FarmGreen, strokeWidth = 2.dp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Real-time counter columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Scanned Count
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = LightSage)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Lidos / Passando", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("${viewModel.sessionScannedAnimals.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = FarmGreen)
                                }
                            }

                            // Missing Count
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = OrangeAlert.copy(alpha = 0.12f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Faltando (No Pasto)", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("${viewModel.sessionMissingAnimals.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrangeAlert)
                                }
                            }
                        }
                    }
                }
            }

            // RFID simulation toolbox
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CustomSlate),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Simulador RFID BSP32 (Hardware UHF Link)", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Selecione qual animal de ${viewModel.gateSourcePaddock} passou no portão para ler a tag RFID:", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.sessionMissingAnimals.isEmpty()) {
                            Text("100% dos animais lidos e contados!", color = Color.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(viewModel.sessionMissingAnimals) { missing ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.simulateRfidScan(missing.rfid)
                                                Toast.makeText(context, "RFID lida: ${missing.rfid}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Brinco: ${missing.brinco}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("Rfid: ${missing.rfid}", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Simular Passagem", color = SoftGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.SensorWindow, contentDescription = null, tint = SoftGold, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scanned result & completion buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.cancelGateSession() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descartar")
                    }

                    Button(
                        onClick = {
                            viewModel.completeGateSession()
                            Toast.makeText(context, "Sessão concluída! Banco de dados de piquetes atualizado.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Mover de Piquete (${viewModel.sessionScannedAnimals.size})", color = Color.White)
                    }
                }
            }
        }
    }
}


// --- SUB-SCREEN: 4. PEDIGREE BREEDING SYSTEM (MATCHING / ANTI-INBREEDING) ---
@Composable
fun PedigreeBreedingScreen(viewModel: HerdViewModel) {
    val animals by viewModel.allAnimals.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Assistente de Cruzamentos e Genética",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Evite a consanguinidade e identifique melhores acasalamentos de ovelhas e bois.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // Selection card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Prever Acasalamento", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Reprodutores Male Selection dropdown
                    var expandSire by remember { mutableStateOf(false) }
                    Text("Reprodutor (Pai)*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(onClick = { expandSire = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (viewModel.breederSireBrinco.isBlank()) "Escolher Reprodutor (Macho)" else "Pai: ${viewModel.breederSireBrinco}")
                        }
                        DropdownMenu(expanded = expandSire, onDismissRequest = { expandSire = false }) {
                            animals.filter { it.condic == "ativo" && (it.sexo == "M" || it.categ == "reprodutor") }.forEach { anim ->
                                DropdownMenuItem(
                                    text = { Text("BRINCO: ${anim.brinco} (${anim.categ})") },
                                    onClick = {
                                        viewModel.breederSireBrinco = anim.brinco
                                        expandSire = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Matrizes Female Selection dropdown
                    var expandDam by remember { mutableStateOf(false) }
                    Text("Matriz (Mãe)*", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box {
                        OutlinedButton(onClick = { expandDam = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (viewModel.breederDamBrinco.isBlank()) "Escolher Matriz (Fêmea)" else "Mãe: ${viewModel.breederDamBrinco}")
                        }
                        DropdownMenu(expanded = expandDam, onDismissRequest = { expandDam = false }) {
                            animals.filter { it.condic == "ativo" && (it.sexo == "F" || it.categ == "matriz") }.forEach { anim ->
                                DropdownMenuItem(
                                    text = { Text("BRINCO: ${anim.brinco} (${anim.categ})") },
                                    onClick = {
                                        viewModel.breederDamBrinco = anim.brinco
                                        expandDam = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetBreedingChecks() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpar")
                        }

                        Button(
                            onClick = {
                                if (viewModel.breederSireBrinco.isBlank() || viewModel.breederDamBrinco.isBlank()) {
                                    Toast.makeText(context, "Selecione o reprodutor e a matriz primeiro!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.checkPedigreeBreeding()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen),
                            modifier = Modifier.weight(2.5f)
                        ) {
                            Text("Analisar Consanguinidade", color = Color.White)
                        }
                    }
                }
            }
        }

        // Compatibility output
        viewModel.compatibilityResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isCompatible) FarmGreen.copy(alpha = 0.12f) else OrangeAlert.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (res.isCompatible) FarmGreen else OrangeAlert)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (res.isCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (res.isCompatible) FarmGreen else OrangeAlert,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (res.isCompatible) "CRUZAMENTO COMPATÍVEL" else "ATENÇÃO: ALTO RISCO",
                                fontWeight = FontWeight.Bold,
                                color = if (res.isCompatible) DarkGreen else OrangeAlert,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = res.reason,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = CustomSlate
                        )

                        if (res.commonAncestors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ancestrais em Comum (Fator Consanguíneo):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 80.dp)
                            ) {
                                items(res.commonAncestors) { anc ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("• Brinco: ", fontSize = 12.sp, color = Color.Gray)
                                        Text(anc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SUB-SCREEN: 5. CONFIGURATION, PADDOCKS & RATEIO COSTS ---
@Composable
fun ConfigAndCostsScreen(viewModel: HerdViewModel) {
    val locals by viewModel.allLocals.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Modal add Paddock controller state
    var isAddingPaddock by remember { mutableStateOf(false) }
    var codLocal by remember { mutableStateOf("") }
    var descLocal by remember { mutableStateOf("") }
    var areaLocal by remember { mutableStateOf("") }
    var sedeLocal by remember { mutableStateOf("") }
    var fixCostLocal by remember { mutableStateOf("") }
    var varCostLocal by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSage)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Piquetes, Custos & Rateios",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "Determine o valor exato gasto por cabeça, distribuindo periodicamente os custos fixos de cercas/campo e variáveis de insumos.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // Expenses rateio action panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FarmGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = SoftGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Ratear Custos de Produção", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "O custo fixo do piquete será dividido pelos animais nele ativos e somado ao custo de cada animal automaticamente. O custo variável será rateado pela permanência.",
                        color = LightSage.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.executeExpensesAllocation()
                            Toast.makeText(context, "Custos fixos e de insumos rateados e atualizados!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = softGoldContainerColorCheck()),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_rateio_costs_button")
                    ) {
                        Text("SIMULAR RATEIO PERIÓDICO AGORA", fontWeight = FontWeight.Bold, color = DarkGreen)
                    }
                }
            }
        }

        // Paddock Lists header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Piquetes Cadastrados", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                if (!isAddingPaddock) {
                    TextButton(onClick = { isAddingPaddock = true }) {
                        Text("+ Adicionar", color = FarmGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add Paddock mini form inline
        if (isAddingPaddock) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cadastrar Novo Piquete / Padoque", fontWeight = FontWeight.Bold, color = CustomSlate)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = codLocal,
                            onValueChange = { if (it.length <= 3) codLocal = it.uppercase() },
                            label = { Text("Código (ex: P53)* - Máximo 3 letras") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = descLocal,
                            onValueChange = { descLocal = it },
                            label = { Text("Descrição (ex: Rotator Brachiaria)*") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = areaLocal,
                                onValueChange = { areaLocal = it },
                                label = { Text("Área (hectares)*") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sedeLocal,
                                onValueChange = { sedeLocal = it },
                                label = { Text("Sede Vinculada*") },
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fixCostLocal,
                                onValueChange = { fixCostLocal = it },
                                label = { Text("Custo Fixo (Cercas/Sede)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = varCostLocal,
                                onValueChange = { varCostLocal = it },
                                label = { Text("Custo Variável (Adubo)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { isAddingPaddock = false }) { Text("Cancelar") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val areaD = areaLocal.toDoubleOrNull() ?: 0.0
                                    val fixCostD = fixCostLocal.toDoubleOrNull() ?: 0.0
                                    val varCostD = varCostLocal.toDoubleOrNull() ?: 0.0

                                    if (codLocal.isNotBlank() && descLocal.isNotBlank() && areaD > 0.0 && sedeLocal.isNotBlank()) {
                                        viewModel.registerLocal(codLocal, descLocal, areaD, sedeLocal, fixCostD, varCostD)
                                        isAddingPaddock = false
                                        codLocal = ""
                                        descLocal = ""
                                        areaLocal = ""
                                        sedeLocal = ""
                                        fixCostLocal = ""
                                        varCostLocal = ""
                                        Toast.makeText(context, "Piquete cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Preencha todos os campos obrigatórios (*)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreen)
                            ) {
                                Text("SALVAR", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Locals card list
        if (locals.isEmpty()) {
            item {
                Text("Sem pastos cadastrados ainda. Clique em Adicionar.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            items(locals) { loc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Text("CÓDIGO: ", fontSize = 12.sp, color = Color.Gray)
                                Text(loc.local_cod, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                            }
                            IconButton(onClick = { viewModel.deleteLocalRecord(loc.local_cod) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir pasto", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                        Text(loc.descricao, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkGreen)
                        Text("Área: ${loc.local_area} hectares | Sede: ${loc.local_sede}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = LightSage)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Estruturas (Cercas)", fontSize = 11.sp, color = Color.Gray)
                                Text("Custo Fixo: R$ ${String.format(Locale.getDefault(), "%.2f", loc.local_custo_fix)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                            }
                            Column {
                                Text("Insumos (Fért.),", fontSize = 11.sp, color = Color.Gray)
                                Text("Custo Var: R$ ${String.format(Locale.getDefault(), "%.2f", loc.local_custo_var)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CustomSlate)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Design helper to avoid warning on M3 color scheme combinations
fun softGoldContainerColorCheck(): Color {
    return SoftGold
}
