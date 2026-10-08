package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitation
import co.edu.unicauca.urbanguard.domain.model.VisitorInvitationStatus
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Tarjeta de una invitación del residente en el historial.
 *
 * Vive en archivo propio para no superar el máximo de funciones por archivo
 * de detekt. Las activas y programadas son pulsables para recuperar el código
 * y volver a compartirlas; las usadas y expiradas se muestran informativas.
 */
@Composable
fun InvitationListItem(
    invitation: VisitorInvitation,
    onShareClick: () -> Unit
) {
    val shareable = invitation.status.isShareable
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgSurface, RoundedCornerShape(16.dp))
            .then(
                if (shareable) Modifier.clickable(onClick = onShareClick)
                else Modifier
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InvitationIconTile()

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invitation.visitorName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )

            Spacer(Modifier.height(6.dp))

            InvitationMetaRow(
                icon = Icons.Default.Group,
                text = invitationGuestText(guestCount = invitation.guestCount)
            )

            Spacer(Modifier.height(6.dp))

            InvitationMetaRow(
                icon = Icons.Default.CalendarMonth,
                text = invitation.visitDate.format(VisitorConstants.DATE_LABEL_FORMAT)
            )

            Spacer(Modifier.height(6.dp))

            InvitationStatusBadge(status = invitation.status)
        }

        if (shareable) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartir",
                tint = UgEmerald,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InvitationIconTile() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(UgInputBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = null,
            tint = UgNavy,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun InvitationStatusBadge(status: VisitorInvitationStatus) {
    val background = (statusBadgeColor(status = status)).copy(alpha = 0.12f)
    Text(
        text = statusBadgeText(status = status),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = statusBadgeColor(status = status),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

private fun statusBadgeText(status: VisitorInvitationStatus): String = when (status) {
    VisitorInvitationStatus.ACTIVE -> "ACTIVA"
    VisitorInvitationStatus.PROGRAMMED -> "PROGRAMADA"
    VisitorInvitationStatus.USED -> "UTILIZADA"
    VisitorInvitationStatus.EXPIRED -> "EXPIRADA"
}

private fun statusBadgeColor(status: VisitorInvitationStatus): Color = when (status) {
    VisitorInvitationStatus.ACTIVE -> UgEmerald
    VisitorInvitationStatus.PROGRAMMED -> UgNavy
    VisitorInvitationStatus.USED -> Color(0xFFF57C00)
    VisitorInvitationStatus.EXPIRED -> UgTextSecondary
}

@Composable
private fun InvitationMetaRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = UgTextSecondary
        )
    }
}

private fun invitationGuestText(guestCount: Int): String =
    if (guestCount == 1) "1 invitado" else "$guestCount invitados"
