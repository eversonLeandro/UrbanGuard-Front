package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.domain.usecase.visitors.GetLastGeneratedPassUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Entrega el pase que acaba de generar el formulario.
 *
 * El pase vive en el repositorio (un @Singleton), no en el ViewModel del
 * formulario, para que esta pantalla no necesite ViewModel compartido ni
 * argumentos de navegación.
 */
@HiltViewModel
class GeneratedPassViewModel @Inject constructor(
    getLastGeneratedPassUseCase: GetLastGeneratedPassUseCase
) : ViewModel() {

    private val _pass = MutableStateFlow(getLastGeneratedPassUseCase())
    val pass: StateFlow<VisitorAuthorization?> = _pass.asStateFlow()
}
