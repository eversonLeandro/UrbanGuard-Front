package co.edu.unicauca.urbanguard.data.remote.api.dto

data class CreatePqrsRequestDto(
    val type: String,
    val subject: String,
    val description: String
)

data class ResidentPqrsCreatedDto(
    val id: String,
    val type: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String
)

data class ResidentPqrsSummaryDto(
    val id: String,
    val type: String,
    val subject: String,
    val status: String,
    val createdAt: String
)

data class ResidentPqrsDetailDto(
    val id: String,
    val type: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val history: List<ResidentPqrsHistoryEntryDto>? = null
)

data class ResidentPqrsHistoryEntryDto(
    val id: String,
    val fromStatus: String,
    val toStatus: String,
    val response: String,
    val createdAt: String
)
