package co.edu.unicauca.urbanguard.domain.repository
import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
interface ConjuntoCodeRepository {
    suspend fun getCode(): Result<ConjuntoCode>
    suspend fun generateCode(): Result<ConjuntoCode>
    suspend fun regenerateCode(): Result<ConjuntoCode>
}