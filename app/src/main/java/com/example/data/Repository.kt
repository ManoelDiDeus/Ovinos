package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class HerdRepository(private val dao: LiveStockDao) {

    // Animals
    val allAnimals: Flow<List<Animal>> = dao.getAllAnimals()
    val allWeights: Flow<List<AnimPeso>> = dao.getAllWeights()
    val allOccurrences: Flow<List<AnimOcor>> = dao.getAllOccurrences()
    val allLocals: Flow<List<Local>> = dao.getAllLocals()
    val allOperators: Flow<List<Operador>> = dao.getAllOperators()

    suspend fun getAnimalByBrinco(brinco: String): Animal? = dao.getAnimalByBrinco(brinco)
    suspend fun getAnimalByRfid(rfid: String): Animal? = dao.getAnimalByRfid(rfid)

    suspend fun insertAnimal(animal: Animal) = dao.insertAnimal(animal)
    suspend fun updateAnimal(animal: Animal) = dao.updateAnimal(animal)
    suspend fun deleteAnimal(brinco: String) = dao.deleteAnimal(brinco)

    suspend fun updateAnimalPaddock(brinco: String, localCod: String?) = dao.updateAnimalPaddock(brinco, localCod)
    suspend fun updateAnimalPeso(brinco: String, peso: Double) = dao.updateAnimalPeso(brinco, peso)

    // Weights
    fun getWeightsForAnimal(brinco: String): Flow<List<AnimPeso>> = dao.getWeightsForAnimal(brinco)
    suspend fun insertWeight(animPeso: AnimPeso) {
        dao.insertWeight(animPeso)
        // Keep animal's current weight in sync
        dao.updateAnimalPeso(animPeso.brinco, animPeso.peso)
    }

    // Occurrences
    fun getOccurrencesForAnimal(brinco: String): Flow<List<AnimOcor>> = dao.getOccurrencesForAnimal(brinco)
    suspend fun insertOccurrence(animOcor: AnimOcor) {
        dao.insertOccurrence(animOcor)
        // Retrieve current cost and add the occurrence expense to animal's lifetime accumulated cost
        val animal = dao.getAnimalByBrinco(animOcor.brinco)
        if (animal != null) {
            val updatedCusto = animal.anim_custo + animOcor.ocor_custo
            dao.updateAnimalCusto(animOcor.brinco, updatedCusto)
        }
    }

    // Paddocks / Locals
    suspend fun getLocalById(localCod: String): Local? = dao.getLocalById(localCod)
    suspend fun insertLocal(local: Local) = dao.insertLocal(local)
    suspend fun updateLocal(local: Local) = dao.updateLocal(local)
    suspend fun deleteLocal(localCod: String) = dao.deleteLocal(localCod)

    // Operators
    suspend fun getOperatorById(operCod: String): Operador? = dao.getOperatorById(operCod)
    suspend fun insertOperator(operador: Operador) = dao.insertOperator(operador)
    suspend fun deleteOperator(operCod: String) = dao.deleteOperator(operCod)

    /**
     * Obtains list of recursive ancestors for an animal up to [depth] generations.
     */
    suspend fun getAncestors(brinco: String, depth: Int = 3): Set<String> {
        if (depth <= 0) return emptySet()
        val anim = dao.getAnimalByBrinco(brinco) ?: return emptySet()
        val ancestors = mutableSetOf<String>()
        anim.pai?.let {
            if (it.isNotBlank()) {
                ancestors.add(it)
                ancestors.addAll(getAncestors(it, depth - 1))
            }
        }
        anim.mae?.let {
            if (it.isNotBlank()) {
                ancestors.add(it)
                ancestors.addAll(getAncestors(it, depth - 1))
            }
        }
        return ancestors
    }

    /**
     * Checks relationship (consanguinity) between two animals to prevent inbreeding.
     * Returns a list of conflicting common ancestors or direct parent relationships.
     */
    suspend fun verifyBreedingCompatibility(sireBrinco: String, damBrinco: String): BreedingCheckResult {
        if (sireBrinco == damBrinco) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Incompatível: O reprodutor e a matriz fornecidos são o mesmo animal.",
                commonAncestors = emptyList()
            )
        }

        val sireObj = dao.getAnimalByBrinco(sireBrinco)
        val damObj = dao.getAnimalByBrinco(damBrinco)

        if (sireObj == null || damObj == null) {
            return BreedingCheckResult(
                isCompatible = true,
                reason = "Compatível (Falta de dados completos nos registros dos pais).",
                commonAncestors = emptyList()
            )
        }

        // Direct parent checks
        if (sireObj.brinco == damObj.pai) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Grave risco de consanguinidade! O reprodutor é Pai da matriz.",
                commonAncestors = listOf(sireBrinco)
            )
        }
        if (sireObj.brinco == damObj.mae) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Incompatível! O reprodutor é Mãe (?) do animal correspondente.",
                commonAncestors = listOf(sireBrinco)
            )
        }
        if (damObj.brinco == sireObj.pai || damObj.brinco == sireObj.mae) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Grave risco de consanguinidade! A matriz é progenitora do reprodutor.",
                commonAncestors = listOf(damBrinco)
            )
        }

        // Shared sibling check
        if (sireObj.pai != null && damObj.pai != null && sireObj.pai == damObj.pai && sireObj.pai.isNotBlank()) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Risco de consanguinidade! Compartilham o mesmo Pai (Meio-irmãos / Irmãos inteiros).",
                commonAncestors = listOf(sireObj.pai)
            )
        }
        if (sireObj.mae != null && damObj.mae != null && sireObj.mae == damObj.mae && sireObj.mae.isNotBlank()) {
            return BreedingCheckResult(
                isCompatible = false,
                reason = "Risco de consanguinidade! Compartilham a mesma Mãe (Meio-irmãos / Irmãos inteiros).",
                commonAncestors = listOf(sireObj.mae)
            )
        }

        // Ancestry overlapping (consanguinity) lookup up to 3 generations
        val sireAncestors = getAncestors(sireBrinco, depth = 3)
        val damAncestors = getAncestors(damBrinco, depth = 3)
        val overlapping = sireAncestors.intersect(damAncestors).toList()

        return if (overlapping.isNotEmpty()) {
            BreedingCheckResult(
                isCompatible = false,
                reason = "Atenção: Consanguinidade detectada! Os animais compartilham ancestrais comuns.",
                commonAncestors = overlapping
            )
        } else {
            BreedingCheckResult(
                isCompatible = true,
                reason = "Compatível! Sem consanguinidade próxima detectada até a 3ª geração.",
                commonAncestors = emptyList()
            )
        }
    }

    /**
     * Allocates fixed and variable expenses from paddocks/locals to the animals grazing in them.
     */
    suspend fun performPaddockCostAllocation() {
        val locals = dao.getAllLocals().firstOrNull() ?: return
        val animals = dao.getAllAnimals().firstOrNull() ?: return

        for (local in locals) {
            val grazingHerd = animals.filter { it.local_cod == local.local_cod && it.condic == "ativo" }
            if (grazingHerd.isNotEmpty()) {
                // Rateio: divide fixed expenses and variable expenses by active animals currently inside
                val perCapitaCost = (local.local_custo_fix + local.local_custo_var) / grazingHerd.size
                for (anim in grazingHerd) {
                    val updatedCusto = anim.anim_custo + perCapitaCost
                    dao.updateAnimalCusto(anim.brinco, updatedCusto)
                }
            }
        }
    }
}

data class BreedingCheckResult(
    val isCompatible: Boolean,
    val reason: String,
    val commonAncestors: List<String>
)
