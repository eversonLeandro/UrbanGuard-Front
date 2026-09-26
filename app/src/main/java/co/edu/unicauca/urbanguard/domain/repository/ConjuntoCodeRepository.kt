package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex

interface ConjuntoCodeRepository {
    suspend fun getCode(conjuntoId: String? = null): Result<ConjuntoCode>
    suspend fun generateCode(conjuntoId: String): Result<ConjuntoCode>
    suspend fun regenerateCode(conjuntoId: String): Result<ConjuntoCode>

    // Crea un conjunto (complex) tras validar el código de activación.
    // POST /api/complexes → 201 con { complexId, membershipId, role }.
    suspend fun createConjunto(
        activationCode: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<CreatedComplex>
}