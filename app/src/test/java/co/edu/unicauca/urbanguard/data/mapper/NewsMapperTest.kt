package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsAttachmentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NewsMapperTest {

    @Test
    fun `news dto maps to domain with attachment null`() {
        val dto = NewsDto(
            announcementId = "a1",
            title = "titulo",
            body = "cuerpo",
            publishedAt = "2026-10-27T10:00:00Z",
            attachment = null
        )
        val domain = dto.toDomain()
        assertEquals("a1", domain.announcementId)
        assertEquals("titulo", domain.title)
        assertEquals("cuerpo", domain.body)
        assertNotNull(domain.publishedAt)
        assertNull(domain.attachment)
    }

    @Test
    fun `news dto maps to domain with attachment`() {
        val dto = NewsDto(
            announcementId = "a2",
            title = "titulo",
            body = "cuerpo",
            publishedAt = "2026-10-27T11:00:00+00:00",
            attachment = NewsAttachmentDto(fileName = "file.pdf", contentType = "application/pdf", sizeBytes = 123)
        )
        val domain = dto.toDomain()
        assertEquals("file.pdf", domain.attachment?.fileName)
        assertEquals("application/pdf", domain.attachment?.contentType)
        assertEquals(123L, domain.attachment?.sizeBytes)
    }

    @Test
    fun `metadata request body uses json utf8 content type`() {
        val body = createMetadataRequestBody("titulo", "cuerpo")
        assertEquals("application/json; charset=utf-8", body.contentType()?.toString())
    }
}
