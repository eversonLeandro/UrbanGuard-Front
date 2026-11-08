package co.edu.unicauca.urbanguard.data.remote.api.dto

import co.edu.unicauca.urbanguard.domain.model.OwnProfile
import java.time.Instant

data class OwnProfileDto(
    val id: String,
    val name: String?,
    val profilePhotoUrl: String?,
    val profilePhotoExpiresAt: String?
) {
    fun toDomain(): OwnProfile = OwnProfile(
        id = id,
        name = name,
        profilePhotoUrl = profilePhotoUrl,
        profilePhotoExpiresAt = profilePhotoExpiresAt?.let(Instant::parse)
    )
}
