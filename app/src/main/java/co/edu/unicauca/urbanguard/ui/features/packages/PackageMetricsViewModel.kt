package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PackageMetrics
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PackageMetricsUiState(
    val isLoading: Boolean = true,
    val metrics: PackageMetrics? = null,
    val errorMessage: String? = null,
    val period: PackageMetricsPeriod = PackageMetricsPeriod.MONTH
)

enum class PackageMetricsPeriod(val label: String) {
    WEEK("7 días"),
    MONTH("30 días"),
    ALL("Todo el periodo")
}

@HiltViewModel
class PackageMetricsViewModel @Inject constructor(
    private val repository: PackageRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackageMetricsUiState())
    val uiState: StateFlow<PackageMetricsUiState> = _uiState.asStateFlow()

    init {
        load(PackageMetricsPeriod.MONTH)
    }

    fun load(period: PackageMetricsPeriod) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = PackageMetricsUiState(
                isLoading = false,
                period = period,
                errorMessage = "Seleccione un conjunto activo"
            )
            return
        }
        val today = LocalDate.now(ZoneOffset.UTC)
        val from = when (period) {
            PackageMetricsPeriod.WEEK -> today.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant().toString()
            PackageMetricsPeriod.MONTH -> today.minusDays(29).atStartOfDay(ZoneOffset.UTC).toInstant().toString()
            PackageMetricsPeriod.ALL -> null
        }
        val to = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString()
        _uiState.value = PackageMetricsUiState(isLoading = true, period = period)
        viewModelScope.launch {
            repository.metrics(complexId, from, if (period == PackageMetricsPeriod.ALL) null else to).fold(
                onSuccess = {
                    _uiState.value = PackageMetricsUiState(
                        isLoading = false,
                        metrics = it,
                        period = period
                    )
                },
                onFailure = {
                    _uiState.value = PackageMetricsUiState(
                        isLoading = false,
                        period = period,
                        errorMessage = it.message ?: "No fue posible cargar las métricas"
                    )
                }
            )
        }
    }
}
