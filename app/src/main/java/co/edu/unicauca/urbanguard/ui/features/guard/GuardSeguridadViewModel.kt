package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GuardSeguridadViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GuardSeguridadUiState())
    val uiState: StateFlow<GuardSeguridadUiState> = _uiState.asStateFlow()
}
