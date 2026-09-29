package co.edu.unicauca.urbanguard.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "QrCodeImage"
private val QR_CORNER_RADIUS: Dp = 16.dp
private val QR_PADDING: Dp = 12.dp
private val QR_DARK_COLOR: Int = 0xFF0F172A.toInt()
private val QR_LIGHT_COLOR: Int = 0xFFFFFFFF.toInt()
private const val PNG_QUALITY = 100

/**
 * Dibuja el contenido de un pase de visitante como imagen QR.
 *
 * El bitmap se genera con ZXing y se memoriza por [qrCode] para no recomponerlo
 * en cada recomposición. Si ZXing rechaza el contenido se registra el motivo y
 * se deja el cuadro vacío en lugar de propagar la excepción a la UI.
 */
@Composable
fun QrCodeImage(
    qrCode: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(qrCode) { createQrBitmap(qrCode) }

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(QR_CORNER_RADIUS))
            .padding(QR_PADDING)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = VisitorConstants.CD_QR_IMAGE,
                modifier = Modifier.size(size)
            )
        }
    }
}

private fun createQrBitmap(qrCode: String): Bitmap? {
    return try {
        val matrix = QRCodeWriter().encode(
            qrCode,
            BarcodeFormat.QR_CODE,
            VisitorConstants.QR_BITMAP_SIZE_PX,
            VisitorConstants.QR_BITMAP_SIZE_PX
        )
        createBitmapFrom(matrix)
    } catch (e: WriterException) {
        Log.e(TAG, "No se pudo generar el QR: ${e.message}", e)
        null
    }
}

private fun createBitmapFrom(matrix: BitMatrix): Bitmap {
    val width = matrix.width
    val height = matrix.height
    val pixels = IntArray(width * height)
    fillPixels(matrix, pixels, width)
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, width, 0, 0, width, height)
    }
}

private fun fillPixels(matrix: BitMatrix, pixels: IntArray, width: Int) {
    for (y in 0 until matrix.height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (matrix[x, y]) QR_DARK_COLOR else QR_LIGHT_COLOR
        }
    }
}

/**
 * Escribe el pase [qrCode] como imagen PNG dentro de `cacheDir/qr` para poder
 * compartirlo con otras apps.
 *
 * El archivo vive en la caché, así que lo regenera Android cuando hace falta;
 * la ruta debe estar declarada en `res/xml/file_paths.xml` para que el
 * `FileProvider` pueda exponerla. Devuelve null si ZXing rechaza el contenido o
 * si no se puede escribir el archivo, y en ese caso se registra el motivo.
 */
fun writeQrBitmapToCache(context: Context, qrCode: String): File? {
    val bitmap = createQrBitmap(qrCode) ?: return null
    val directory = File(context.cacheDir, VisitorConstants.QR_CACHE_DIR)
    if (!directory.exists() && !directory.mkdirs()) {
        Log.e(TAG, "No se pudo crear ${VisitorConstants.QR_CACHE_DIR} en la caché")
        return null
    }
    val file = File(directory, VisitorConstants.SHARE_FILE_NAME)
    return try {
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
        }
        file
    } catch (e: IOException) {
        Log.e(TAG, "No se pudo escribir el PNG del QR: ${e.message}", e)
        null
    }
}
