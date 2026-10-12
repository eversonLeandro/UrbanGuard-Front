package co.edu.unicauca.urbanguard.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.domain.usecase.auth.LogoutUseCase
import co.edu.unicauca.urbanguard.domain.usecase.auth.RestoreSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orquestador de sesión a nivel de navegación.
 *
 * - [initialRoute]: restaura la sesión persistida y decide la pantalla inicial
 *   (la del rol, o LOGIN si no hay credenciales válidas/renovables). Al
 *   restaurar con conjunto activo se registra el token push (arranque en frío).
 * - [logout]: desregistra el token push de esta cuenta y luego borra las
 *   credenciales cifradas y la sesión en memoria antes de permitir la
 *   navegación de vuelta al Login.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val pushRegistrar: PushRegistrar
) : ViewModel() {

    suspend fun initialRoute(): String {
        val role = restoreSessionUseCase()?.role
        if (role != null) {
            pushRegistrar.registerActiveConjunto()
        }
        return roleToDestination(role)
    }

    fun logout(onComplete: () -> Unit) {
        pushRegistrar.unregisterCurrent {
            viewModelScope.launch {
                logoutUseCase()
                onComplete()
            }
        }
    }
}
