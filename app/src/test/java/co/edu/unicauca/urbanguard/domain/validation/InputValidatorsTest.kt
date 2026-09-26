package co.edu.unicauca.urbanguard.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorsTest {

    @Test
    fun `a well formed email is accepted`() {
        assertTrue(InputValidators.isValidEmail("residente@urbanguard.com"))
    }

    @Test
    fun `emails that contains checks used to accept are now rejected`() {
        // Antes el registro solo comprobaba `contains("@")`, por lo que estas
        // entradas mal formadas llegaban hasta Auth0.
        listOf("a@b", "@@", "correo@", "@dominio.com", "espacio ici@x.com", "").forEach { email ->
            assertFalse("No debería aceptarse: '$email'", InputValidators.isValidEmail(email))
        }
    }

    @Test
    fun `a compliant password has no violations`() {
        assertTrue(InputValidators.passwordPolicyViolations("Clave1234").isEmpty())
        assertEquals(null, InputValidators.passwordPolicyMessage("Clave1234"))
    }

    @Test
    fun `a short password is reported as such`() {
        val violations = InputValidators.passwordPolicyViolations("Ab1")

        assertEquals(listOf("debe tener al menos 8 caracteres"), violations)
    }

    @Test
    fun `every missing rule is reported so the user can fix them all at once`() {
        val violations = InputValidators.passwordPolicyViolations("corta")

        assertEquals(3, violations.size)
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres y debe incluir al menos una letra mayúscula " +
                "y debe incluir al menos un número.",
            InputValidators.passwordPolicyMessage("corta")
        )
    }
}
