package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animal")
data class Animal(
    @PrimaryKey val brinco: String, // PK: 5 chars max, unique, required
    val rfid: String, // TID RFID code, unique, required
    val categ: String, // Category: borrego, cordeiro, matriz, reprodutor, castrado, descarte
    val dat_nasc: String, // Birthdate formatted (e.g., YYYY-MM-DD)
    val peso: Double, // Current weight in kg
    val pai: String?, // Sire earring (FK, optional)
    val mae: String?, // Dam earring (FK, optional)
    val foto: String?, // Local Uri or base64 JPG path for animal photo
    val sexo: String, // "M" or "F"
    val local_cod: String?, // Current Paddock/Pasture location code (FK, optional)
    val condic: String = "ativo", // Condition: ativo, inativo, abatido, morto, vendido
    val anim_C_inic: Double, // Initial cost
    val anim_custo: Double // Total accumulated lifetime cost
)

@Entity(tableName = "anim_peso")
data class AnimPeso(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brinco: String, // Animal earring FK
    val data_hora: Long, // Peso timestamp
    val peso: Double, // Weight in kg
    val ipc: String // Index 1-5 (1=muito magro, 2=magro, 3=bom, 4=gordo, 5=muito gordo)
)

@Entity(tableName = "anim_ocor")
data class AnimOcor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brinco: String, // Animal earring FK
    val data_hora: Long, // Occurrences timestamp
    val descricao: String, // 50 chars max description
    val ocor_custo: Double // Expense/Cost associated with occurrence
)

@Entity(tableName = "local")
data class Local(
    @PrimaryKey val local_cod: String, // PK: 3 chars max code
    val descricao: String, // 30 chars max required
    val local_area: Double, // Size in hectares
    val local_sede: String, // 20 chars max required
    val local_custo_fix: Double, // Permanent structure (fences, weeding) amortization cost
    val local_custo_var: Double // Seasonal variable cost (adubos, seeds, etc.)
)

@Entity(tableName = "operador")
data class Operador(
    @PrimaryKey val oper_cod: String, // PK: 3 chars max
    val oper_nome: String, // 30 chars name
    val oper_niv: String, // admin "A" or operator "O"
    val oper_senha: String // 8 chars max password
)
