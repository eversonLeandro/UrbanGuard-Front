package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsAttachmentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsMetadataDto
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.model.NewsAttachment
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.OffsetDateTime

private val gson = Gson()
private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

fun NewsDto.toDomain(): News {
    return News(
        announcementId = announcementId,
        title = title,
        body = body,
        publishedAt = OffsetDateTime.parse(publishedAt),
        attachment = attachment?.toDomain()
    )
}

fun NewsAttachmentDto.toDomain(): NewsAttachment {
    return NewsAttachment(
        fileName = fileName,
        contentType = contentType,
        sizeBytes = sizeBytes
    )
}

fun NewsMetadataDto.toMetadataRequestBody(): RequestBody {
    val json = gson.toJson(this)
    return json.toRequestBody(jsonMediaType)
}

fun createMetadataRequestBody(title: String, body: String): RequestBody {
    val metadata = NewsMetadataDto(title = title, body = body)
    return metadata.toMetadataRequestBody()
}
