package co.edu.unicauca.urbanguard.core.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "PhotoCapture"

/** Crea el archivo donde la cámara del sistema guardará la foto. */
fun createPhotoFile(context: Context): File {
    val dir = File(context.cacheDir, PHOTO_DIR).apply { mkdirs() }
    return File.createTempFile(PHOTO_PREFIX, PHOTO_SUFFIX, dir)
}

private fun photoUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

/**
 * Devuelve una acción para tomar foto con la app de cámara del sistema.
 *
 * La foto queda temporalmente en caché propia y se entrega al formulario
 * para subirla al servidor. Si no hay cámara disponible, avisa por [onError].
 */
@Composable
// El catch amplio cubre lo que puede fallar al abrir la cámara del sistema
// (sin app de cámara, FileProvider, IO del caché): ante cualquier fallo se
// avisa y se sigue sin foto, sin romper el flujo.
@Suppress("TooGenericExceptionCaught")
fun rememberPhotoCapture(
    onCaptured: (File) -> Unit,
    onError: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<File?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            pending?.takeIf { it.exists() }?.let(onCaptured)
        } else {
            pending?.delete()
        }
        pending = null
    }
    return {
        try {
            val file = createPhotoFile(context)
            pending = file
            launcher.launch(photoUri(context, file))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo abrir la cámara", e)
            pending = null
            onError()
        }
    }
}

private const val PHOTO_DIR = "incidents"
private const val PHOTO_PREFIX = "novedad_"
private const val PHOTO_SUFFIX = ".jpg"
