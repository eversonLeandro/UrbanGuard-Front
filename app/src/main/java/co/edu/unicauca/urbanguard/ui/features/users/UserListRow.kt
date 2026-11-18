package co.edu.unicauca.urbanguard.ui.features.users

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
import androidx.compose.material.icons.filled.Lock
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
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun UserRow(
    state: UserListUiState,
    user: User,
    viewModel: UserListViewModel
) {
    val isSelf = user.userId == state.myUserId
    val isBusy = state.actionInProgressUserId == user.userId

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
            UserAvatar(user, isSelf)
            Spacer(Modifier.width(12.dp))
            UserDetails(user, isSelf)
            Spacer(Modifier.width(8.dp))
            UserStatusAction(user, isSelf, isBusy, viewModel)
        }
    }
}

@Composable
private fun UserAvatar(user: User, isSelf: Boolean) {
    val badgeColor = if (user.activo) UgEmerald else UgAmber
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isSelf) badgeColor.copy(alpha = 0.25f) else badgeColor.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials(user),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelf) UgNavy else badgeColor
        )
    }
}

@Composable
private fun RowScope.UserDetails(user: User, isSelf: Boolean) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            user.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgNavy,
            maxLines = 1
        )
        Text(
            user.email ?: user.userId,
            fontSize = 11.5.sp,
            color = UgTextSecondary,
            maxLines = 1
        )
        Text(
            when {
                isSelf -> "Tu cuenta · sin desactivación"
                user.activo -> "Activa"
                else -> "Desactivada"
            },
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                isSelf -> UgNavy
                user.activo -> UgEmerald
                else -> UgAmber
            }
        )
    }
}

@Composable
private fun UserStatusAction(
    user: User,
    isSelf: Boolean,
    isBusy: Boolean,
    viewModel: UserListViewModel
) {
    when {
        isBusy -> CircularProgressIndicator(
            color = UgEmerald,
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        user.activo && isSelf -> Icon(
            Icons.Default.Lock,
            contentDescription = "Cuenta propia protegida",
            tint = UgNavy,
            modifier = Modifier.size(22.dp)
        )
        user.activo -> IconButton(
            onClick = { viewModel.onDeactivateDialogChange(user) },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = "Desactivar cuenta",
                tint = UgErrorText,
                modifier = Modifier.size(22.dp)
            )
        }
        else -> IconButton(
            onClick = { viewModel.onReactivate(user) },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = "Reactivar cuenta",
                tint = UgEmerald,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** Iniciales del avatar: primera letra del nombre y del apellido, A/B por defecto. */
private fun initials(user: User): String {
    val source = listOfNotNull(
        user.nombres?.trim()?.firstOrNull(),
        user.apellidos?.trim()?.firstOrNull()
    ).joinToString("")
    return source
        .uppercase()
        .ifEmpty { "UG" }
}
