package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

/** Avatar compartido, con fallback inmediato si una URL temporal falla. */
@Composable
fun ProfileAvatar(
    name: String,
    photoUrl: String?,
    size: Dp,
    background: Color,
    onImageError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var failed by remember(photoUrl) { mutableStateOf(false) }
    Box(modifier = modifier.size(size).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Text(name.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "U",
            color = Color.White, fontSize = (size.value * 0.4f).sp, fontWeight = FontWeight.Bold)
        if (!photoUrl.isNullOrBlank() && !failed) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape),
                onError = { failed = true; onImageError(photoUrl) }
            )
        }
    }
}

@Composable
fun ProfileRefreshOnResume(photoUrl: String?, expiresAt: Instant?, onResume: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, onResume) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(photoUrl, expiresAt) {
        if (photoUrl != null && expiresAt != null) {
            val untilRefresh = Duration.between(Instant.now(), expiresAt.minusSeconds(45)).toMillis()
            if (untilRefresh > 0) delay(untilRefresh)
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) onResume()
            val untilExpiry = Duration.between(Instant.now(), expiresAt).toMillis()
            if (untilExpiry > 0) delay(untilExpiry)
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) onResume()
        }
    }
}
