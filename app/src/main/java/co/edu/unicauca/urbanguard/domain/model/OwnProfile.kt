package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

data class OwnProfile(
    val id: String,
    val name: String?,
    val profilePhotoUrl: String?,
    val profilePhotoExpiresAt: Instant?
)
