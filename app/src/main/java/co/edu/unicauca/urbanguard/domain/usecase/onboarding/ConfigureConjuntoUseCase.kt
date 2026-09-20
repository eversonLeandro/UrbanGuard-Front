package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.core.common.OnboardingBypass
import co.edu.unicauca.urbanguard.domain.model.CreatedComplex
import co.edu.unicauca.urbanguard.domain.model.MembershipRole
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

/**
 * Crea el conjunto (complex) con los datos del formulario de registro.
 *
 * POST /api/complexes → 201 Created con { complexId, membershipId, role }.
 * El activationCode ya fue validado en el paso ActivateConjunto y viaja
 * por navegación hasta esta pantalla (argumento de ruta).
 *
 * El conjunto se CREA aquí (no se actualiza por ID): reemplaza al antiguo
 * ConfigureConjuntoUseCase (PUT onboarding/conjunto/{conjuntoId}).
 */
class ConfigureConjuntoUseCase @Inject constructor(
    private val repository: ConjuntoCodeRepository
) {
    suspend operator fun invoke(
        activationCode: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<CreatedComplex> {
        if (OnboardingBypass.ENABLED) {
            // Valor simulado del 201 Created de POST /api/complexes.
            return Result.success(
                CreatedComplex(
                    complexId = "bypass-complex-001",
                    membershipId = "bypass-membership-001",
                    roles = listOf(MembershipRole.ADMIN)
                )
            )
        } else {
            return repository.createConjunto(
                activationCode = activationCode,
                nombre = nombre,
                direccion = direccion,
                ciudad = ciudad,
                totalUnidades = totalUnidades
            )
        }
    }
}