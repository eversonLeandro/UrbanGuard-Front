package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.components.FilterDatePickerDialog
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import java.time.LocalDate

private val LIST_PADDING = 20.dp
private val LIST_GAP = 12.dp
private val LIST_GAP_SMALL = 6.dp
private val LIST_CORNER = 16.dp
private val LIST_EMPTY_ICON = 72.dp
private val LIST_EMPTY_ICON_SIZE = 32.dp

/**
 * Pantalla de la pestaña Visitantes: historial de invitaciones del residente
 * con su estado, y acceso a la creación de una nueva.
 *
 * El historial llega del backend mediante
 * `GET api/complexes/{complexId}/visitor-invitations`. Pulsar una invitación
 * activa o programada abre su código para volver a compartirlo.
 */
@Composable
fun VisitorPassListScreen(
    onGenerateNewClick: () -> Unit,
    onInvitationClick: (String) -> Unit = {},
    viewModel: VisitorPassListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onRefresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Visitantes",
        )

        if (uiState.isLoading) {
            LoadingState()
        } else {
            PassListContent(
                uiState = uiState,
                onGenerateNewClick = onGenerateNewClick,
                onRetryClick = viewModel::onRefresh,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onDateFilterChange = viewModel::onDateFilterChange,
                onInvitationClick = { invitation ->
                    viewModel.selectInvitation(invitation)
                    onInvitationClick(invitation.invitationId)
                }
            )
        }
    }
}

@Composable
private fun VisitorListFilters(
    uiState: VisitorPassListUiState,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (LocalDate?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LIST_PADDING),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchFilterField(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.weight(1f).height(50.dp)
        )

        DateFilterField(
            date = uiState.dateFilter,
            onShowDatePicker = { showDatePicker = true },
            onClearDate = { onDateFilterChange(null) }
        )
    }

    if (showDatePicker) {
        FilterDatePickerDialog(
            initialDate = uiState.dateFilter,
            onDateSelected = onDateFilterChange,
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun SearchFilterField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar nombre", fontSize = 13.sp, color = UgDisabledText) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = UgTextSecondary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = androidx.compose.ui.graphics.Color.White,
            unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = UgBorder
        ),
        modifier = modifier
    )
}

@Composable
private fun PassListContent(
    uiState: VisitorPassListUiState,
    onGenerateNewClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (LocalDate?) -> Unit,
    onInvitationClick: (VisitorInvitation) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))

        VisitorListFilters(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onDateFilterChange = onDateFilterChange
        )

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage, onRetryClick = onRetryClick)
                uiState.isEmpty -> EmptyState()
                else -> PassesList(passes = uiState.filteredPasses, onInvitationClick = onInvitationClick)
            }
        }

        VisitorActionButton(
            text = "Generar nuevo QR",
            onClick = onGenerateNewClick,
            modifier = Modifier.padding(
                start = LIST_PADDING,
                end = LIST_PADDING,
                top = 12.dp,
                bottom = LIST_PADDING
            )
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = LIST_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(LIST_EMPTY_ICON)
                .background(UgInputBg, RoundedCornerShape(LIST_CORNER)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(LIST_EMPTY_ICON_SIZE)
            )
        }

        Spacer(Modifier.height(LIST_GAP))

        Text(
            text = VisitorConstants.LIST_EMPTY_TITLE,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary
        )

        Spacer(Modifier.height(LIST_GAP_SMALL))

        Text(
            text = VisitorConstants.LIST_EMPTY_SUBTITLE,
            fontSize = 13.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = LIST_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRetryClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(UgNavy)
            )
        ) {
            Text("Reintentar", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PassesList(
    passes: List<VisitorInvitation>,
    onInvitationClick: (VisitorInvitation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LIST_PADDING,
            end = LIST_PADDING,
            bottom = LIST_PADDING
        ),
        verticalArrangement = Arrangement.spacedBy(LIST_GAP)
    ) {
        items(passes, key = { it.invitationId }) { invitation ->
            InvitationListItem(
                invitation = invitation,
                onShareClick = { onInvitationClick(invitation) }
            )
        }
    }
}
