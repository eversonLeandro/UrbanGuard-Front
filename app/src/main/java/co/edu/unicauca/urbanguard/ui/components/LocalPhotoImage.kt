package co.edu.unicauca.urbanguard.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Foto guardada en el equipo, reducida en memoria para miniaturas.
 *
 * Sin dependencias externas: decodifica con `inSampleSize` para no cargar
 * la foto completa. Si no hay ruta o el archivo se perdió, muestra un
 * marcador neutro del mismo tamaño.
 */
@Composable
fun LocalPhotoImage(
    path: String?,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(path) { path?.let { decodeScaled(it) } }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(UgInputBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// El catch amplio cubre decodificaciones corruptas o archivos borrados a
// mitad de lectura: ante cualquier fallo se muestra el marcador neutro.
@Suppress("TooGenericExceptionCaught")
private fun decodeScaled(path: String, maxSize: Int = 512): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while (largest / (sample * 2) >= maxSize) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(path, options)
    } catch (e: Exception) {
        Log.w("LocalPhotoImage", "No se pudo decodificar $path", e)
        null
    }
}
