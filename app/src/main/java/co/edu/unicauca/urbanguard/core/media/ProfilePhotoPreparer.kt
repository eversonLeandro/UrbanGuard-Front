package co.edu.unicauca.urbanguard.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

class PreparedProfilePhoto internal constructor(val file: File) {
    val part: MultipartBody.Part = MultipartBody.Part.createFormData(
        "image", "profile.jpg", object : RequestBody() {
            override fun contentType() = "image/jpeg".toMediaType()
            override fun contentLength(): Long = file.length()
            // Impide que OkHttp reenvíe un PUT cuyo cuerpo ya empezó a transmitirse.
            override fun isOneShot(): Boolean = true
            override fun writeTo(sink: BufferedSink) {
                FileInputStream(file).use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        sink.write(buffer, 0, count)
                    }
                }
            }
        }
    )

    fun cleanup() { file.delete() }
}

@Singleton
// Las funciones pequeñas aíslan validación, orientación, compresión y limpieza.
@Suppress("TooManyFunctions")
open class ProfilePhotoPreparer @Inject constructor(@ApplicationContext private val context: Context) {
    private val resolver get() = context.contentResolver

    fun cameraOutput(): Pair<Uri, File> {
        val dir = File(context.cacheDir, PROFILE_DIR).apply { mkdirs() }
        val file = File.createTempFile("capture_", ".jpg", dir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return uri to file
    }

    fun discardCameraOutput(uri: Uri?) {
        val file = uri?.lastPathSegment?.substringAfterLast('/')
            ?.let { File(context.cacheDir, "$PROFILE_DIR/$it") }
        val ownDir = File(context.cacheDir, PROFILE_DIR).canonicalPath + File.separator
        if (file?.canonicalPath?.startsWith(ownDir) == true) {
            file.delete()
        }
    }

    // Límite de entrada: todos los fallos de Android/archivo se entregan como Result.
    @Suppress("TooGenericExceptionCaught")
    open suspend fun prepare(uri: Uri): Result<PreparedProfilePhoto> {
        var prepared: PreparedProfilePhoto? = null
        return try {
            val value = withContext(Dispatchers.IO) {
                prepareBlocking(uri).also { prepared = it }
            }
            Result.success(value)
        } catch (e: CancellationException) {
            prepared?.cleanup()
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("ThrowsCount")
    private fun prepareBlocking(uri: Uri): PreparedProfilePhoto {
        val declared = validateMetadata(uri)
        val dir = File(context.cacheDir, PROFILE_DIR).apply { mkdirs() }
        val source = File.createTempFile("source_", ".tmp", dir)
        try {
            copySource(uri, source)
            val bitmap = decodeValidated(source, declared)
            val output = File.createTempFile("upload_", ".jpg", dir)
            var success = false
            try {
                writeJpeg(bitmap, output)
                if (output.length() > MAX_BYTES) throw AppException(AppError.ImageTooLarge())
                success = true
                return PreparedProfilePhoto(output)
            } finally {
                bitmap.recycle()
                if (!success) output.delete()
            }
        } catch (e: IOException) {
            throw AppException(AppError.Validation("No se pudo leer la imagen seleccionada."))
                .apply { initCause(e) }
        } catch (e: SecurityException) {
            throw AppException(AppError.Validation("No se pudo leer la imagen seleccionada."))
                .apply { initCause(e) }
        } finally {
            source.delete()
        }
    }

    private fun validateMetadata(uri: Uri): String? {
        val declared = resolver.getType(uri)?.lowercase()
        if (declared != null && declared !in TYPES) throw AppException(AppError.UnsupportedImage())
        val size = resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else null
        }
        if (size != null && size > MAX_BYTES) throw AppException(AppError.ImageTooLarge())
        return declared
    }

    // El anidamiento garantiza cierre de ambos streams incluso al superar el límite.
    @Suppress("NestedBlockDepth", "ThrowsCount")
    private fun copySource(uri: Uri, source: File) {
        val input = resolver.openInputStream(uri)
            ?: throw AppException(AppError.Validation("No se pudo abrir la imagen."))
        input.use { stream ->
            FileOutputStream(source).use { target ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_BYTES) throw AppException(AppError.ImageTooLarge())
                    target.write(buffer, 0, count)
                }
            }
        }
        if (source.length() == 0L) throw AppException(AppError.Validation("La imagen está vacía."))
    }

    @Suppress("ThrowsCount")
    private fun decodeValidated(source: File, declared: String?): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.path, bounds)
        val detected = bounds.outMimeType?.lowercase()
        if (detected !in TYPES || bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw AppException(AppError.UnsupportedImage())
        }
        if (bounds.outWidth.toLong() * bounds.outHeight > MAX_PIXELS ||
            (declared != null && !equivalent(declared, detected!!))
        ) throw AppException(AppError.UnsupportedImage())
        val sample = sampleSize(bounds.outWidth, bounds.outHeight)
        val bitmap = BitmapFactory.decodeFile(source.path, BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }) ?: throw AppException(AppError.UnsupportedImage())
        val orientation = ExifInterface(source.path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val oriented = orient(bitmap, orientation)
        if (oriented !== bitmap) bitmap.recycle()
        return oriented
    }

    private fun writeJpeg(bitmap: Bitmap, output: File) {
        // El JPEG generado elimina EXIF; el blanco evita fondo negro en PNG transparente.
        val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        try {
            Canvas(opaque).apply { drawColor(Color.WHITE); drawBitmap(bitmap, 0f, 0f, null) }
            for (quality in listOf(85, 75, 65)) {
                FileOutputStream(output).use { opaque.compress(Bitmap.CompressFormat.JPEG, quality, it) }
                if (output.length() <= MAX_BYTES) break
            }
        } finally {
            opaque.recycle()
        }
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (maxOf(width / sample, height / sample) > TARGET_SIZE) sample *= 2
        return sample
    }

    private fun orient(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(270f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun equivalent(first: String, second: String): Boolean =
        first == second || setOf(first, second) == setOf("image/heic", "image/heif") ||
            setOf(first, second) == setOf("image/jpg", "image/jpeg")

    companion object {
        private const val PROFILE_DIR = "profile"
        private const val TARGET_SIZE = 2048
        private const val MAX_BYTES = 5L * 1024 * 1024
        private const val MAX_PIXELS = 64_000_000L
        private val TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp", "image/heic", "image/heif")
    }
}
