package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GuardHomeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardHomeUiState())
    val uiState: StateFlow<GuardHomeUiState> = _uiState.asStateFlow()

    init {
        val session = sessionManager.session.value
        _uiState.update {
            it.copy(
                userName = session?.name ?: session?.userId ?: "guardia@urbanguard.com"
            )
        }
    }
}