package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.OnboardingStatusDto
import co.edu.unicauca.urbanguard.domain.model.OnboardingStatus

fun OnboardingStatusDto.toDomain(): OnboardingStatus = OnboardingStatus(
    hasConjunto = hasConjunto,
    conjuntoId = conjuntoId
)