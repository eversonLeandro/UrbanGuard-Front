package co.edu.unicauca.urbanguard.domain.usecase.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import javax.inject.Inject

/**
 * Entrega el último pase QR generado en la sesión, para que la pantalla de
 * resultado pueda mostrarlo sin argumentos de navegación ni ViewModel
 * compartido. Es una lectura en memoria, por eso no es suspendida.
 */
class GetLastGeneratedPassUseCase @Inject constructor(
    private val repository: VisitorRepository
) {
    operator fun invoke(): VisitorAuthorization? = repository.lastGeneratedPass()
}
