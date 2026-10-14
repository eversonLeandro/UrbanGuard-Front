package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate

/**
 * Detalle de un turno (HU-032): veredicto de meta y desglose de rondas.
 *
 * Tocar el vigilante de una ronda abre su resumen de desempeño.
 */
@Composable
fun PatrolShiftDetailScreen(
    onNavigateBack: () -> Unit,
    onGuardClick: (String) -> Unit,
    viewModel: PatrolShiftDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Detalle del turno",
            onNavigateBack = onNavigateBack
        )

        DetailContent(
            uiState = uiState,
            onRetry = viewModel::onRetry,
            onGuardClick = onGuardClick
        )
    }
}

@Composable
private fun DetailContent(
    uiState: PatrolShiftDetailUiState,
    onRetry: () -> Unit,
    onGuardClick: (String) -> Unit
) {
    val error = uiState.errorMessage
    val detail = uiState.detail
    when {
        uiState.isLoading -> DetailLoading()
        error != null -> DetailError(message = error, onRetryClick = onRetry)
        detail == null -> DetailError(
            message = "No hay información del turno.",
            onRetryClick = onRetry
        )
        else -> DetailBody(
            detail = detail,
            shiftType = uiState.shiftType,
            date = uiState.date,
            onGuardClick = onGuardClick
        )
    }
}

@Composable
private fun DetailBody(
    detail: PatrolShiftDetail,
    shiftType: PatrolShiftType,
    date: LocalDate,
    onGuardClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            ShiftDetailHeader(detail = detail, shiftType = shiftType, date = date)
        }

        item(key = "rounds-title") {
            Text(
                text = "Rondas del turno",
                fontSize = 15.sp,
                color = UgNavy
            )
        }

        if (detail.rounds.isEmpty()) {
            item(key = "rounds-empty") {
                Text(
                    text = "Este turno no tiene rondas registradas.",
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
            }
        } else {
            items(detail.rounds) { round ->
                ShiftRoundRow(round = round, onGuardClick = onGuardClick)
            }
            item(key = "rounds-bottom") { Spacer(Modifier.height(4.dp)) }
        }
    }
}
