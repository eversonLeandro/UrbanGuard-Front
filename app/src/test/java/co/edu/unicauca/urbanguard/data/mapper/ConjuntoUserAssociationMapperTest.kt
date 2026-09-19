package co.edu.unicauca.urbanguard.data.mapper

import co.edu.unicauca.urbanguard.data.remote.api.dto.ComplexSummaryDto
import co.edu.unicauca.urbanguard.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitarios del mapper [ComplexSummaryDto.toDomain].
 *
 * Verifica la correspondencia exacta de los campos del backend al modelo
 * de dominio y el comportamiento del parseUserRole para todos los casos
 * relevantes del backend (ADMIN, RESIDENT, GUARD) y el caso de rol desconocido.
 */
class ConjuntoUserAssociationMapperTest {

    private fun makeDto(role: String) = ComplexSummaryDto(
        id = "complex-uuid-123",
        name = "Conjunto Test",
        address = "Calle 45 #23-10",
        city = "Bogotá",
        numberOfUnits = 50,
        role = role,
        unit = "Apto 301"
    )

    @Test
    fun toDomain_mapsIdToConjuntoId() {
        val domain = makeDto("ADMIN").toDomain()
        assertEquals("complex-uuid-123", domain.conjuntoId)
    }

    @Test
    fun toDomain_mapsNameToNombre() {
        val domain = makeDto("RESIDENT").toDomain()
        assertEquals("Conjunto Test", domain.nombre)
    }

    @Test
    fun toDomain_adminRole_mapsToAdminUserRole() {
        assertEquals(UserRole.ADMIN, makeDto("ADMIN").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_residentRole_mapsToResidentUserRole() {
        assertEquals(UserRole.RESIDENT, makeDto("RESIDENT").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_guardRole_mapsToGuardUserRole() {
        assertEquals(UserRole.GUARD, makeDto("GUARD").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_unknownRole_defaultsToResident() {
        // Rol desconocido o null — el mapper hace RESIDENT por defecto (decisión de diseño)
        assertEquals(UserRole.RESIDENT, makeDto("UNKNOWN_ROLE").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_caseInsensitiveRole_admin() {
        // El backend podría enviar variantes de capitalización
        assertEquals(UserRole.ADMIN, makeDto("admin").toDomain().roleEnConjunto)
        assertEquals(UserRole.ADMIN, makeDto("Admin").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_caseInsensitiveRole_guard() {
        assertEquals(UserRole.GUARD, makeDto("guard").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_caseInsensitiveRole_resident() {
        assertEquals(UserRole.RESIDENT, makeDto("resident").toDomain().roleEnConjunto)
    }

    @Test
    fun toDomain_nullableFieldsDoNotAffectMapping() {
        // address, city, numberOfUnits y unit son nullable — no deben romper el mapper
        val dto = ComplexSummaryDto(
            id = "uuid-456",
            name = "Conjunto Sin Datos",
            address = null,
            city = null,
            numberOfUnits = null,
            role = "RESIDENT",
            unit = null
        )
        val domain = dto.toDomain()
        assertEquals("uuid-456", domain.conjuntoId)
        assertEquals("Conjunto Sin Datos", domain.nombre)
        assertEquals(UserRole.RESIDENT, domain.roleEnConjunto)
    }
}
