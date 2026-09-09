package co.edu.unicauca.urbanguard.domain.repository
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
interface ConjuntoCodeRepository {
    suspend fun getCode(conjuntoId: String? = null): Result<ConjuntoCode>
    suspend fun generateCode(conjuntoId: String? = null): Result<ConjuntoCode>
    suspend fun regenerateCode(conjuntoId: String? = null): Result<ConjuntoCode>

    // Actualiza los datos de un conjunto YA existente por su ID.
    // El conjunto se crea al validar el código de activación (paso Activate),
    // por lo que ya no se envía codigoActivacion aquí.
    suspend fun updateConjunto(
        conjuntoId: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<Unit>
}