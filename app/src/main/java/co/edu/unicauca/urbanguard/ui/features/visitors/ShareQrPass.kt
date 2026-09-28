package co.edu.unicauca.urbanguard.ui.features.visitors

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization
import co.edu.unicauca.urbanguard.ui.components.writeQrBitmapToCache
import java.io.File

private const val TAG = "ShareQrPass"
private const val MIME_PNG = "image/png"

/**
 * Abre el diálogo clásico de compartir con la imagen del pase y un texto que la
 * acompaña, siguiendo el mismo patrón que usa la pantalla del código del
 * conjunto (`ACTION_SEND` + `Intent.createChooser`).
 *
 * La imagen se expone como `content://` mediante el `FileProvider` declarado en
 * el manifiesto, porque compartir rutas de archivo entre apps está prohibido
 * desde Android 7.
 */
fun shareVisitorPass(context: Context, pass: VisitorAuthorization) {
    val intent = buildShareIntent(context, pass)
    context.startActivity(Intent.createChooser(intent, VisitorConstants.SHARE_CHOOSER_TITLE))
}

private fun buildShareIntent(context: Context, pass: VisitorAuthorization): Intent {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = MIME_PNG
        putExtra(Intent.EXTRA_TEXT, shareText(pass))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val imageUri = qrImageUri(context, pass.qrCode)
    if (imageUri != null) {
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
    } else {
        Log.e(TAG, "El pase se compartirá solo como texto: no se pudo generar la imagen")
    }
    return intent
}

private fun qrImageUri(context: Context, qrCode: String): Uri? {
    val file: File = writeQrBitmapToCache(context = context, qrCode = qrCode) ?: return null
    return FileProvider.getUriForFile(context, fileProviderAuthority(context), file)
}

private fun fileProviderAuthority(context: Context): String = "${context.packageName}.fileprovider"

private fun shareText(pass: VisitorAuthorization): String =
    "Pase de visitante de ${pass.visitorName} para ${pass.guestCount} en " +
        "${pass.validDate.format(VisitorConstants.DATE_LABEL_FORMAT)}. " +
        "Presenta este código en la portería."
