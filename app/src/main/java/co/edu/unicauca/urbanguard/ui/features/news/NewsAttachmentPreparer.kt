package co.edu.unicauca.urbanguard.ui.features.news

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.data.remote.api.AnnouncementImageUpload
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal object NewsAttachmentPreparer {

    val allowedTypes = setOf("application/pdf", "image/jpeg", "image/png")

    sealed interface Result {
        data class Ready(val body: RequestBody?, val fileName: String?) : Result
        data class Failed(val message: String) : Result
    }

    fun sanitize(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return if (cleaned.length > NewsConstants.ATTACHMENT_NAME_MAX_LENGTH) {
            cleaned.take(NewsConstants.ATTACHMENT_NAME_MAX_LENGTH)
        } else {
            cleaned
        }
    }

    fun queryColumn(context: Context, uri: Uri, column: String): String? {
        val cursor = context.contentResolver.query(uri, arrayOf(column), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(column)
                if (idx >= 0 && !it.isNull(idx)) {
                    return it.getString(idx)
                }
            }
        }
        return null
    }

    @Suppress("SwallowedException")
    suspend fun prepare(
        context: Context,
        uri: Uri?,
        mime: String?,
        name: String?,
        sizeHint: Long?
    ): Result {
        val size = sizeHint ?: uri?.let { queryColumn(context, it, OpenableColumns.SIZE)?.toLongOrNull() }
        if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
            return Result.Failed(NewsConstants.ATTACH_TOO_LARGE)
        }
        val target = uri ?: return Result.Ready(null, null)
        return try {
            if (mime == "application/pdf") {
                preparePdf(context, target, name)
            } else {
                prepareImage(context, target, name)
            }
        } catch (e: IOException) {
            Result.Failed(NewsConstants.ATTACH_OPEN_ERROR)
        } catch (e: SecurityException) {
            Result.Failed(NewsConstants.ATTACH_OPEN_ERROR)
        }
    }

    private suspend fun preparePdf(context: Context, uri: Uri, name: String?): Result {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: return Result.Failed(NewsConstants.ATTACH_OPEN_ERROR)
        if (bytes.size.toLong() > NewsConstants.ATTACHMENT_MAX_BYTES) {
            return Result.Failed(NewsConstants.ATTACH_TOO_LARGE)
        }
        return Result.Ready(bytes.toRequestBody("application/pdf".toMediaTypeOrNull()), name)
    }

    private suspend fun prepareImage(context: Context, uri: Uri, name: String?): Result {
        val prepared = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                AnnouncementImageUpload.cacheAndAsRequestBody(
                    context.cacheDir,
                    stream,
                    name ?: "imagen"
                )
            }
        } ?: return Result.Failed(NewsConstants.ATTACH_OPEN_ERROR)
        if (prepared.body.contentLength() > NewsConstants.ATTACHMENT_MAX_BYTES) {
            return Result.Failed(NewsConstants.ATTACH_TOO_LARGE)
        }
        return Result.Ready(prepared.body, prepared.fileName)
    }
}
