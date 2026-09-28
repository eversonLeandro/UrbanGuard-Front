package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.ui.components.QrCodeImage
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val LIST_QR_SIZE = 56.dp
private val LIST_PADDING = 20.dp
private val LIST_GAP = 12.dp
private val LIST_GAP_SMALL = 6.dp
private val LIST_CORNER = 16.dp
private val LIST_EMPTY_ICON = 72.dp
private val LIST_EMPTY_ICON_SIZE = 32.dp
private val LIST_META_ICON = 14.dp
private val LIST_ICON_GAP = 6.dp

/**
 * Pantalla de la pestaña Visitantes: historial de pases QR del conjunto y
 * acceso a la creación de uno nuevo.
 *
 * El historial llega del backend mediante `GET api/visitors/authorizations`.
 * Mientras ese endpoint no exista, la lista llega vacía y se muestra el estado
 * vacío, no un error.
 */
@Composable
fun VisitorPassListScreen(
    onGenerateNewClick: () -> Unit,
    viewModel: VisitorPassListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                onGenerateNewClick = onGenerateNewClick
            )
        }
    }
}

@Composable
private fun PassListContent(
    uiState: VisitorPassListUiState,
    onGenerateNewClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VisitorActionButton(
            text = "Generar nuevo QR",
            onClick = onGenerateNewClick,
            modifier = Modifier.padding(
                start = LIST_PADDING,
                end = LIST_PADDING,
                top = LIST_PADDING
            )
        )

        Spacer(Modifier.height(LIST_PADDING))

        when {
            uiState.errorMessage != null -> ErrorState(message = uiState.errorMessage)
            uiState.isEmpty -> EmptyState()
            else -> PassesList(passes = uiState.passes)
        }
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
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = LIST_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PassesList(passes: List<VisitorAuthorization>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LIST_PADDING,
            end = LIST_PADDING,
            bottom = LIST_PADDING
        ),
        verticalArrangement = Arrangement.spacedBy(LIST_GAP)
    ) {
        items(passes) { pass -> PassListItem(pass = pass) }
    }
}

@Composable
private fun PassListItem(pass: VisitorAuthorization) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgSurface, RoundedCornerShape(LIST_CORNER))
            .padding(LIST_GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QrCodeImage(qrCode = pass.qrCode, size = LIST_QR_SIZE)

        Spacer(Modifier.width(LIST_GAP))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pass.visitorName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )

            Spacer(Modifier.height(LIST_ICON_GAP))

            PassMetaRow(
                icon = Icons.Default.Group,
                text = guestCountText(guestCount = pass.guestCount)
            )

            Spacer(Modifier.height(LIST_ICON_GAP))

            PassMetaRow(
                icon = Icons.Default.CalendarMonth,
                text = pass.validDate.format(VisitorConstants.DATE_LABEL_FORMAT)
            )
        }
    }
}

@Composable
private fun PassMetaRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(LIST_META_ICON)
        )
        Spacer(Modifier.width(LIST_ICON_GAP))
        Text(
            text = text,
            fontSize = 12.sp,
            color = UgTextSecondary
        )
    }
}

private fun guestCountText(guestCount: Int): String =
    if (guestCount == 1) "1 invitado" else "$guestCount invitados"
