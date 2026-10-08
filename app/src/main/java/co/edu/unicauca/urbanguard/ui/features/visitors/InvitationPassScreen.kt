package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val DETAIL_QR_SIZE = 240.dp
private val DETAIL_PADDING = 20.dp
private val DETAIL_GAP = 8.dp
private val DETAIL_GAP_LARGE = 24.dp

/**
 * Pantalla de recompartir: muestra el QR recuperado de una invitación activa
 * o programada y permite compartirlo, reutilizando el mismo compartir de
 * [GeneratedPassScreen].
 */
@Composable
fun InvitationPassScreen(
    onBackClick: () -> Unit,
    viewModel: InvitationPassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Compartir pase",
            onNavigateBack = onBackClick
        )

        val currentPass = uiState.pass
        when {
            uiState.isLoading -> InvitationPassLoading()
            currentPass != null -> InvitationPassContent(
                pass = currentPass,
                onShareClick = { shareVisitorPass(context, currentPass) }
            )
            else -> InvitationPassError(
                message = uiState.errorMessage ?: "No se pudo recuperar el código.",
                onRetryClick = viewModel::onRetry,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun InvitationPassLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun InvitationPassContent(
    pass: VisitorAuthorization,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DETAIL_PADDING, vertical = DETAIL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color.White
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pass.visitorName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgTextPrimary
                )

                Spacer(Modifier.height(DETAIL_GAP))

                Text(
                    text = "Muestra este código en la portería para autorizar el ingreso.",
                    fontSize = 13.sp,
                    color = UgTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .background(UgBackground, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    QrCodeImage(qrCode = pass.qrCode, size = DETAIL_QR_SIZE)
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Válido desde:",
                    fontSize = 12.sp,
                    color = UgTextSecondary
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = pass.validDate.format(VisitorConstants.DATE_LABEL_FORMAT),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgTextPrimary
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        VisitorActionButton(
            text = "Compartir QR",
            onClick = onShareClick
        )
    }
}

@Composable
private fun InvitationPassError(
    message: String,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DETAIL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(DETAIL_GAP_LARGE))

        VisitorActionButton(
            text = "Reintentar",
            onClick = onRetryClick
        )

        Spacer(Modifier.height(DETAIL_GAP))

        VisitorActionButton(
            text = "Volver",
            onClick = onBackClick
        )
    }
}
