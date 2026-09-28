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
    fun `los telefonos que se escriben a mano se aceptan`() {
        listOf(
            "3001234567",
            "+57 300 123 4567",
            "(300) 123-4567",
            "300-123-4567"
        ).forEach { phone ->
            assertTrue("Debería aceptarse: '$phone'", InputValidators.isValidPhone(phone))
        }
    }

    @Test
    fun `un telefono con letras o simbolos se rechaza`() {
        // El mismo conjunto de caracteres que acepta `CreateGuardRequest` en el
        // backend: si aqui se permitiera una letra, el 400 llegaria desde el servidor.
        listOf("300-abc-4567", "llamar al 300", "300#45", "300.123.4567", "").forEach { phone ->
            assertFalse("No debería aceptarse: '$phone'", InputValidators.isValidPhone(phone))
        }
    }

    @Test
    fun `un telefono mas largo que el maximo del backend se rechaza`() {
        val atLimit = "9".repeat(InputValidators.PHONE_MAX_LENGTH)
        val overLimit = "9".repeat(InputValidators.PHONE_MAX_LENGTH + 1)

        assertTrue(InputValidators.isValidPhone(atLimit))
        assertFalse(InputValidators.isValidPhone(overLimit))
    }

    @Test
    fun `los espacios sobrantes no cuentan para el limite de longitud`() {
        val padded = " ".repeat(10) + "9".repeat(InputValidators.PHONE_MAX_LENGTH) + " ".repeat(10)

        assertTrue(InputValidators.isValidPhone(padded))
    }

    @Test
    fun `un nombre acepta espacios pero no puede quedar vacio`() {
        assertTrue(InputValidators.isValidName("Carlos Andrés"))
        assertFalse(InputValidators.isValidName("   "))
        assertFalse(InputValidators.isValidName(""))
    }

    @Test
    fun `un nombre mas largo que el maximo del backend se rechaza`() {
        assertTrue(InputValidators.isValidName("a".repeat(InputValidators.NAME_MAX_LENGTH)))
        assertFalse(InputValidators.isValidName("a".repeat(InputValidators.NAME_MAX_LENGTH + 1)))
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
