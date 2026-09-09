package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

/**
 * DEPRECADO: este UseCase hacía el POST de creación del conjunto enviando
 * codigoActivacion. El diseño aprobado cambia el flujo:
 *   - el conjunto se crea al validar el código de activación (paso Activate);
 *   - RegisterConjuntoScreen ahora usa ConfigureConjuntoUseCase (PUT por ID).
 *
 * Se conserva readaptado (delega en updateConjunto) únicamente para no romper
 * compilación, pero NO debe usarse. Eliminar cuando se confirme que
 * ConfigureConjuntoUseCase es el único camino de actualización.
 */
@Deprecated("Reemplazado por ConfigureConjuntoUseCase (actualización del conjunto por ID)")
class RegisterConjuntoUseCase @Inject constructor(
    private val repository: ConjuntoCodeRepository
) {
    suspend operator fun invoke(
        conjuntoId: String,
        nombre: String,
        direccion: String,
        ciudad: String,
        totalUnidades: Int
    ): Result<Unit> {
        return repository.updateConjunto(
            conjuntoId = conjuntoId,
            nombre = nombre,
            direccion = direccion,
            ciudad = ciudad,
            totalUnidades = totalUnidades
        )
    }
}
