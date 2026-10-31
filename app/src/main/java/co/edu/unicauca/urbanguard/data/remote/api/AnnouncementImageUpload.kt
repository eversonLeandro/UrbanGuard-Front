package co.edu.unicauca.urbanguard.data.remote.api

import java.io.File
import java.io.InputStream
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody

object AnnouncementImageUpload {

    private val jpeg = "image/jpeg".toMediaType()
    private val png = "image/png".toMediaType()

    data class Prepared(
        val body: RequestBody,
        val fileName: String,
        val mediaType: MediaType
    )

    fun cacheAndAsRequestBody(cacheDir: File, input: InputStream, suggestedName: String): Prepared? {
        val dir = File(cacheDir, "announcement_uploads").apply { mkdirs() }
        val raw = File.createTempFile("comunicado_", ".bin", dir)
        raw.outputStream().use { output -> input.copyTo(output) }
        val mediaType = detectMediaType(raw) ?: run {
            raw.delete()
            return null
        }
        val extension = if (mediaType == png) "png" else "jpg"
        val fileName = withExtension(suggestedName, extension)
        val cached = File(dir, "comunicado_${System.nanoTime()}.$extension")
        if (!raw.renameTo(cached)) {
            raw.copyTo(cached, overwrite = true)
            raw.delete()
        }
        return Prepared(cached.asRequestBody(mediaType), fileName, mediaType)
    }

    fun detectMediaType(file: File): MediaType? {
        val header = file.inputStream().use { stream -> stream.readNBytes(8) }
        return detectMediaType(header)
    }

    fun detectMediaType(header: ByteArray): MediaType? {
        if (header.size >= 8 && hasMagic(header, 0x89, 0x50, 0x4E, 0x47)) {
            return png
        }
        if (hasMagic(header, 0xFF, 0xD8, 0xFF)) {
            return jpeg
        }
        return null
    }

    private fun hasMagic(header: ByteArray, vararg magic: Int): Boolean {
        if (header.size < magic.size) return false
        return magic.indices.all { index -> header[index] == magic[index].toByte() }
    }

    fun withExtension(name: String, extension: String): String {
        val cleaned = name.trim().ifBlank { "imagen" }
        val dot = cleaned.lastIndexOf('.')
        val base = if (dot > 0) cleaned.substring(0, dot) else cleaned
        return "$base.$extension"
    }
}
