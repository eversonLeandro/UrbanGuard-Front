package co.edu.unicauca.urbanguard.core.common

/**
 * Flag central para el bypass temporal del onboarding.
 *
 * Activa respuestas quemadas (hardcoded) para los endpoints que el backend real
 * todavía no expone:
 *   - Validar código de activación y crear conjunto (retorna conjuntoId).
 *   - Actualizar conjunto por ID (PUT onboarding/conjunto/{conjuntoId}).
 *
 * El resto del flujo que ya es real (generación de código de vinculación,
 * POST onboarding/code/generate) NO pasa por este flag.
 *
 * Para quitar el bypass:
 *   1. Poner ENABLED = false.
 *   2. Descomentar la lógica real marcada con "// DESCOMENTAR CUANDO ESTÉ EL BACKEND"
 *      en ActivateConjuntoUseCase y ConfigureConjuntoUseCase.
 */
object OnboardingBypass {
    const val ENABLED: Boolean = true

    // Valor quemado que actúa como PK del conjunto mientras no exista
    // el endpoint real de validación de activación.
    const val BYPASS_CONJUNTO_ID: String = "999-DEV-BYPASS"
}
