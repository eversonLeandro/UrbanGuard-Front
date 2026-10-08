package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val PASS_QR_SIZE = 240.dp
private val PASS_PADDING = 20.dp
private val PASS_GAP = 8.dp
private val PASS_GAP_LARGE = 24.dp
private val PASS_CORNER = 12.dp
private val PASS_META_ICON = 18.dp
private val PASS_META_GAP = 10.dp

/**
 * Pantalla de resultado: muestra el pase recién generado y permite compartirlo.
 *
 * La acción principal es "Compartir", que abre el diálogo clásico del sistema
 * con la imagen del QR y un texto que la acompaña.
 */
@Composable
fun GeneratedPassScreen(
    onBackClick: () -> Unit,
    onGenerateAnotherClick: () -> Unit,
    viewModel: GeneratedPassViewModel = hiltViewModel()
) {
    val pass by viewModel.pass.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Pase generado",
            onNavigateBack = onBackClick
        )

        val currentPass = pass
        if (currentPass == null) {
            MissingPassContent(onBackClick = onBackClick)
        } else {
            PassContent(
                pass = currentPass,
                onShareClick = { shareVisitorPass(context, currentPass) },
                onGenerateAnotherClick = onGenerateAnotherClick
            )
        }
    }
}

@Composable
private fun PassContent(
    pass: VisitorAuthorization,
    onShareClick: () -> Unit,
    onGenerateAnotherClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PASS_PADDING, vertical = PASS_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
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

                Spacer(Modifier.height(PASS_GAP))

                Text(
                    text = "Muestra este código en la portería para autorizar el ingreso.",
                    fontSize = 13.sp,
                    color = UgTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(PASS_GAP_LARGE))

                Box(
                    modifier = Modifier
                        .background(UgBackground, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    QrCodeImage(qrCode = pass.qrCode, size = PASS_QR_SIZE)
                }

                Spacer(Modifier.height(PASS_GAP_LARGE))

                PassSummary(pass = pass)
            }
        }

        Spacer(Modifier.height(PASS_GAP_LARGE))

        VisitorActionButton(
            text = "Compartir QR",
            onClick = onShareClick
        )

        Spacer(Modifier.height(PASS_GAP))

        androidx.compose.material3.OutlinedButton(
            onClick = onGenerateAnotherClick,
            shape = RoundedCornerShape(10.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = co.edu.unicauca.urbanguard.ui.theme.UgNavy
            ),
            border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(co.edu.unicauca.urbanguard.ui.theme.UgNavy)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Generar otro pase", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun PassSummary(pass: VisitorAuthorization) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgInputBg, RoundedCornerShape(PASS_CORNER))
            .padding(PASS_PADDING),
        verticalArrangement = Arrangement.spacedBy(PASS_GAP)
    ) {
        PassSummaryRow(
            icon = Icons.Default.Group,
            text = guestCountText(pass.guestCount)
        )
        PassSummaryRow(
            icon = Icons.Default.CalendarMonth,
            text = pass.validDate.format(VisitorConstants.DATE_LABEL_FORMAT)
        )
    }
}

@Composable
private fun PassSummaryRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UgEmerald,
            modifier = Modifier.size(PASS_META_ICON)
        )
        Spacer(Modifier.width(PASS_META_GAP))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary
        )
    }
}

/**
 * Se muestra cuando no hay pase generado en la sesión, por ejemplo si el
 * proceso se recreó y se volvió a entrar directo a esta pantalla.
 */
@Composable
private fun MissingPassContent(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PASS_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No hay ningún pase generado todavía.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )

            Spacer(Modifier.height(PASS_GAP_LARGE))

            VisitorActionButton(
                text = "Volver",
                onClick = onBackClick
            )
        }
    }
}

private fun guestCountText(guestCount: Int): String =
    if (guestCount == 1) "1 invitado" else "$guestCount invitados"
