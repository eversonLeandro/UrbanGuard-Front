package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Fila de vigilante con avatar de iniciales sobre el color del estado, igual
 * que las filas de la gestión de usuarios. Desactivar = Block (rojo), reactivar
 * = LockOpen (esmeralda); ambos por icono en vez del botón ancho anterior.
 */
@Composable
internal fun GuardRow(
    guard: GuardMembership,
    isBusy: Boolean,
    onChangeStatus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuardAvatar(guard)
            Spacer(Modifier.width(12.dp))
            GuardDetails(guard)
            Spacer(Modifier.width(8.dp))
            GuardStatusAction(guard, isBusy, onChangeStatus)
        }
    }
}

@Composable
private fun GuardAvatar(guard: GuardMembership) {
    val badgeColor = if (guard.active) UgEmerald else UgAmber
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(badgeColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials(guard.fullName),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = badgeColor
        )
    }
}

@Composable
private fun RowScope.GuardDetails(guard: GuardMembership) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            guard.fullName,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgNavy,
            maxLines = 1
        )
        Text(
            guard.email ?: guard.membershipId,
            fontSize = 11.5.sp,
            color = UgTextSecondary,
            maxLines = 1
        )
        Text(
            if (guard.active) "Activo en este conjunto" else "Inactivo en este conjunto",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (guard.active) UgEmerald else UgAmber
        )
    }
}

@Composable
private fun GuardStatusAction(
    guard: GuardMembership,
    isBusy: Boolean,
    onChangeStatus: () -> Unit
) {
    when {
        isBusy -> CircularProgressIndicator(
            color = UgEmerald,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        guard.active -> IconButton(
            onClick = onChangeStatus,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = "Inactivar vigilante",
                tint = UgErrorText,
                modifier = Modifier.size(22.dp)
            )
        }
        else -> IconButton(
            onClick = onChangeStatus,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = "Reactivar vigilante",
                tint = UgEmerald,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun initials(fullName: String): String {
    val source = fullName.split(" ")
        .mapNotNull { it.trim().firstOrNull() }
        .take(2)
        .joinToString("")
    return source.uppercase().ifEmpty { "UG" }
}
