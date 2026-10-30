package co.edu.unicauca.urbanguard.domain.model

data class NewsAttachment(
    val fileName: String?,
    val contentType: String?,
    val sizeBytes: Long?,
    val imageUrl: String? = null
)
