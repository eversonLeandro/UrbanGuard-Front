package co.edu.unicauca.urbanguard.data.remote.api.dto

data class UpdateUnitRequestDto(
    val identifier: String
)

data class UpdateUnitResponseDto(
    val id: String? = null,
    val identifier: String? = null
)
