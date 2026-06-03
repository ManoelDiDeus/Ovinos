package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveStockDao {

    // --- ANIMAL OPERATIONS ---
    @Query("SELECT * FROM animal ORDER BY brinco ASC")
    fun getAllAnimals(): Flow<List<Animal>>

    @Query("SELECT * FROM animal WHERE brinco = :brinco LIMIT 1")
    suspend fun getAnimalByBrinco(brinco: String): Animal?

    @Query("SELECT * FROM animal WHERE rfid = :rfid LIMIT 1")
    suspend fun getAnimalByRfid(rfid: String): Animal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: Animal)

    @Update
    suspend fun updateAnimal(animal: Animal)

    @Query("DELETE FROM animal WHERE brinco = :brinco")
    suspend fun deleteAnimal(brinco: String)

    @Query("SELECT * FROM animal WHERE local_cod = :localCod")
    fun getAnimalsByLocation(localCod: String): Flow<List<Animal>>

    @Query("UPDATE animal SET local_cod = :localCod WHERE brinco = :brinco")
    suspend fun updateAnimalPaddock(brinco: String, localCod: String?)

    @Query("UPDATE animal SET anim_custo = :newCusto WHERE brinco = :brinco")
    suspend fun updateAnimalCusto(brinco: String, newCusto: Double)

    @Query("UPDATE animal SET peso = :newPeso WHERE brinco = :brinco")
    suspend fun updateAnimalPeso(brinco: String, newPeso: Double)


    // --- ANIM_PESO OPERATIONS ---
    @Query("SELECT * FROM anim_peso ORDER BY data_hora DESC")
    fun getAllWeights(): Flow<List<AnimPeso>>

    @Query("SELECT * FROM anim_peso WHERE brinco = :brinco ORDER BY data_hora DESC")
    fun getWeightsForAnimal(brinco: String): Flow<List<AnimPeso>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(animPeso: AnimPeso)

    @Query("DELETE FROM anim_peso WHERE id = :id")
    suspend fun deleteWeight(id: Int)


    // --- ANIM_OCOR OPERATIONS ---
    @Query("SELECT * FROM anim_ocor ORDER BY data_hora DESC")
    fun getAllOccurrences(): Flow<List<AnimOcor>>

    @Query("SELECT * FROM anim_ocor WHERE brinco = :brinco ORDER BY data_hora DESC")
    fun getOccurrencesForAnimal(brinco: String): Flow<List<AnimOcor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(animOcor: AnimOcor)


    // --- LOCAL OPERATIONS ---
    @Query("SELECT * FROM local ORDER BY local_cod ASC")
    fun getAllLocals(): Flow<List<Local>>

    @Query("SELECT * FROM local WHERE local_cod = :localCod LIMIT 1")
    suspend fun getLocalById(localCod: String): Local?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocal(local: Local)

    @Update
    suspend fun updateLocal(local: Local)

    @Query("DELETE FROM local WHERE local_cod = :localCod")
    suspend fun deleteLocal(localCod: String)


    // --- OPERADOR OPERATIONS ---
    @Query("SELECT * FROM operador ORDER BY oper_cod ASC")
    fun getAllOperators(): Flow<List<Operador>>

    @Query("SELECT * FROM operador WHERE oper_cod = :operCod LIMIT 1")
    suspend fun getOperatorById(operCod: String): Operador?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operador: Operador)

    @Query("DELETE FROM operador WHERE oper_cod = :operCod")
    suspend fun deleteOperator(operCod: String)
}
