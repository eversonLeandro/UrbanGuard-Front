package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.core.common.OnboardingBypass
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

/**
 * Actualiza los datos de un conjunto YA existente por su ID (PUT).
 *
 * El conjunto se crea al validar el código de activación (paso ActivateConjunto),
 * por lo que aquí solo se envían los datos (nombre, dirección, ciudad, unidades)
 * y NO codigoActivacion (ese código murió en el paso de Activate).
 */
class ConfigureConjuntoUseCase @Inject constructor(
    private val repository: ConjuntoCodeRepository
) {
    suspend operator fun invoke(
        conjuntoId: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<Unit> {
        if (OnboardingBypass.ENABLED) {
            // Valor quemado: el conjunto "ya existe" tras el bypass de Activate,
            // por lo que el update se considera exitoso para habilitar la
            // navegación a Home sin backend.
            return Result.success(Unit)
        } else {
            // DESCOMENTAR CUANDO ESTÉ EL BACKEND
            // Llama al endpoint real `PUT onboarding/conjunto/{conjuntoId}`.
            // return repository.updateConjunto(
            //     conjuntoId = conjuntoId,
            //     nombre = nombre,
            //     direccion = direccion,
            //     ciudad = ciudad,
            //     totalUnidades = totalUnidades
            // )
            return Result.failure(Exception("Bypass desactivado y endpoint real no implementado todavía"))
        }
    }
}
