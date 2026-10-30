package co.edu.unicauca.urbanguard.data.remote.api

import java.io.ByteArrayInputStream
import java.nio.file.Files
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementImageUploadTest {

    @Test
    fun `detects png and jpeg magic`() {
        assertEquals(
            "image/png".toMediaType(),
            AnnouncementImageUpload.detectMediaType(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        )
        assertEquals(
            "image/jpeg".toMediaType(),
            AnnouncementImageUpload.detectMediaType(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        )
        assertNull(AnnouncementImageUpload.detectMediaType(byteArrayOf(0x25, 0x50, 0x44, 0x46)))
    }

    @Test
    fun `writes cache file and uses asRequestBody with extension`() {
        val dir = Files.createTempDirectory("announcement-upload").toFile()
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3)
        val prepared = AnnouncementImageUpload.cacheAndAsRequestBody(
            dir,
            ByteArrayInputStream(png),
            "foto"
        )
        assertNotNull(prepared)
        assertEquals("foto.png", prepared!!.fileName)
        assertEquals("image/png".toMediaType(), prepared.mediaType)
        assertEquals("image/png".toMediaType(), prepared.body.contentType())
        assertTrue(dir.resolve("announcement_uploads").listFiles()?.any { it.extension == "png" } == true)
    }

    @Test
    fun `withExtension replaces mismatch`() {
        assertEquals("foto.jpg", AnnouncementImageUpload.withExtension("foto.png", "jpg"))
    }
}
