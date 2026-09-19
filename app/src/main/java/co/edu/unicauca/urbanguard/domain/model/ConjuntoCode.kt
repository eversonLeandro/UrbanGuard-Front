package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

data class ConjuntoCode(
    val code: String,
    val expiresAt: Instant
)
