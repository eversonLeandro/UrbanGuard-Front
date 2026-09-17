package co.edu.unicauca.urbanguard.core.common

/**
 * Flag central para el bypass temporal del onboarding.
 *
 * Activa respuestas quemadas (hardcoded) para los endpoints que el backend real
 * todavía no expone:
 *   - Verificar código de activación y crear conjunto (POST activations/verifications → 204).
 *   - Crear conjunto / complex con datos del formulario (POST complexes → 201).
 *
 * El resto del flujo que ya es real (generación de código de vinculación,
 * POST onboarding/code/generate) NO pasa por este flag.
 *
 * Para quitar el bypass:
 *   1. Poner ENABLED = false.
 *   2. Descomentar la lógica real marcada con "// DESCOMENTAR CUANDO ESTÉ EL BACKEND"
 *      en ActivateConjuntoUseCase, ConfigureConjuntoUseCase, OnboardingRepositoryImpl
 *      y ConjuntoCodeRepositoryImpl.
 */
object OnboardingBypass {
    const val ENABLED: Boolean = true
}