package co.edu.unicauca.urbanguard.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.GraphicsMode
import okio.Buffer
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfilePhotoPreparerTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val preparer get() = ProfilePhotoPreparer(context)

    @Test fun `JPEG is prepared as one shot multipart and temporary is cleaned`() = runTest {
        val source = image(Bitmap.CompressFormat.JPEG)
        val prepared = preparer.prepare(Uri.fromFile(source)).getOrThrow()
        assertTrue(prepared.file.exists())
        assertTrue(prepared.file.length() in 1..(5L * 1024 * 1024))
        assertTrue(prepared.part.body.isOneShot())
        val multipart = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)
            .addPart(prepared.part).build()
        assertEquals(1, multipart.parts.size)
        assertTrue(prepared.part.headers?.get("Content-Disposition")?.contains("name=\"image\"") == true)
        val transmitted = Buffer()
        prepared.part.body.writeTo(transmitted)
        assertEquals(prepared.file.length(), transmitted.size)
        prepared.cleanup()
        assertFalse(prepared.file.exists())
        source.delete()
    }

    @Test fun `PNG transparency is converted to JPEG`() = runTest {
        val source = image(Bitmap.CompressFormat.PNG)
        val prepared = preparer.prepare(Uri.fromFile(source)).getOrThrow()
        assertEquals("image/jpeg", BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
            BitmapFactory.decodeFile(prepared.file.path, it)
        }.outMimeType)
        prepared.cleanup()
        source.delete()
    }

    @Test fun `WebP is accepted and converted to upload JPEG`() = runTest {
        val source = image(Bitmap.CompressFormat.WEBP_LOSSY)
        val prepared = preparer.prepare(Uri.fromFile(source)).getOrThrow()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(prepared.file.path, bounds)
        assertEquals("image/jpeg", bounds.outMimeType)
        prepared.cleanup()
        source.delete()
    }

    @Test fun `oversized and corrupt inputs fail without output`() = runTest {
        val large = File.createTempFile("large", ".jpg", context.cacheDir)
        large.outputStream().use { out ->
            val chunk = ByteArray(8192)
            repeat(641) { out.write(chunk) }
        }
        val error = preparer.prepare(Uri.fromFile(large)).exceptionOrNull() as AppException
        assertTrue(error.error is AppError.ImageTooLarge)
        large.delete()

        val corrupt = File.createTempFile("corrupt", ".jpg", context.cacheDir).apply { writeText("not image") }
        val corruptError = preparer.prepare(Uri.fromFile(corrupt)).exceptionOrNull() as AppException
        assertTrue(corruptError.error is AppError.UnsupportedImage)
        corrupt.delete()
    }

    @Test fun `EXIF orientation is applied and not copied to output`() = runTest {
        val source = image(Bitmap.CompressFormat.JPEG)
        ExifInterface(source.path).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            saveAttributes()
        }
        val prepared = preparer.prepare(Uri.fromFile(source)).getOrThrow()
        val bitmap = BitmapFactory.decodeFile(prepared.file.path)
        assertEquals(20, bitmap.width)
        assertEquals(40, bitmap.height)
        assertTrue(ExifInterface(prepared.file.path)
            .getAttribute(ExifInterface.TAG_ORIENTATION) != ExifInterface.ORIENTATION_ROTATE_90.toString())
        bitmap.recycle()
        prepared.cleanup()
        source.delete()
    }

    @Test fun `EXIF horizontal reflection is applied`() = runTest {
        val source = File.createTempFile("mirror", ".jpg", context.cacheDir)
        val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)
        for (x in 0 until 40) for (y in 0 until 20) {
            bitmap.setPixel(x, y, if (x < 20) Color.RED else Color.BLUE)
        }
        FileOutputStream(source).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        bitmap.recycle()
        ExifInterface(source.path).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_FLIP_HORIZONTAL.toString())
            saveAttributes()
        }
        val prepared = preparer.prepare(Uri.fromFile(source)).getOrThrow()
        val output = BitmapFactory.decodeFile(prepared.file.path)
        val left = output.getPixel(5, 10)
        val right = output.getPixel(35, 10)
        assertTrue(Color.blue(left) > Color.red(left))
        assertTrue(Color.red(right) > Color.blue(right))
        output.recycle()
        prepared.cleanup()
        source.delete()
    }

    private fun image(format: Bitmap.CompressFormat): File {
        val extension = when (format) {
            Bitmap.CompressFormat.PNG -> ".png"
            Bitmap.CompressFormat.WEBP_LOSSY -> ".webp"
            else -> ".jpg"
        }
        val file = File.createTempFile("photo", extension, context.cacheDir)
        val bitmap = Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.GREEN)
        FileOutputStream(file).use { bitmap.compress(format, 90, it) }
        bitmap.recycle()
        return file
    }
}
