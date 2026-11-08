package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateOwnerTenantRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateUnitResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.OwnerTenantResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialResidentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentialUnitDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdateUnitResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.service.ResidentialApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.OwnerTenantDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ResidentialRepositoryImplTest {

    @Test
    fun `getCensus envia el conjunto y conserva unidades vacias`() = runTest {
        val api = FakeResidentialApi(
            units = listOf(
                ResidentialUnitDto(
                    id = "unit-1",
                    identifier = "101",
                    residents = listOf(ResidentialResidentDto("user-1", "Ana Pérez"))
                ),
                ResidentialUnitDto(id = "unit-2", identifier = "102", residents = emptyList())
            )
        )

        val result = ResidentialRepositoryImpl(api).getCensus("complex-1")

        assertTrue(result.isSuccess)
        assertEquals("complex-1", api.lastComplexId)
        assertEquals("Ana Pérez", result.getOrThrow().first().residents.single().fullName)
        assertTrue(result.getOrThrow().last().residents.isEmpty())
    }

    @Test
    fun `getCensus sin conjunto retorna validacion sin llamar la API`() = runTest {
        val api = FakeResidentialApi()

        val result = ResidentialRepositoryImpl(api).getCensus("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is co.edu.unicauca.urbanguard.domain.model.AppException)
        assertEquals(0, api.calls)
    }

    @Test
    fun `getCensus mapea residentes censales sin userId y con su tipo`() = runTest {
        val api = FakeResidentialApi(
            units = listOf(
                ResidentialUnitDto(
                    id = "unit-1",
                    identifier = "101",
                    residents = listOf(
                        ResidentialResidentDto(
                            userId = null,
                            fullName = "Ana Pérez",
                            type = "OWNER"
                        )
                    )
                )
            )
        )

        val result = ResidentialRepositoryImpl(api).getCensus("complex-1")

        val resident = result.getOrThrow().single().residents.single()
        assertEquals(null, resident.userId)
        assertEquals(ResidentType.OWNER, resident.type)
    }

    @Test
    fun `getCensus rechaza respuestas incompletas`() = runTest {
        val api = FakeResidentialApi(
            units = listOf(ResidentialUnitDto(id = "unit-1", identifier = null, residents = emptyList()))
        )

        val result = ResidentialRepositoryImpl(api).getCensus("complex-1")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as AppException
        assertTrue(exception.error is AppError.Unexpected)
    }

    @Test
    fun `registerUnit envia el conjunto y mapea la respuesta`() = runTest {
        val api = FakeResidentialApi(
            created = CreateUnitResponseDto(
                id = "unit-3",
                identifier = "103",
                numberOfUnits = 3
            )
        )

        val result = ResidentialRepositoryImpl(api).registerUnit("complex-1")

        assertTrue(result.isSuccess)
        assertEquals("complex-1", api.lastCreateComplexId)
        assertEquals("unit-3", result.getOrThrow().id)
        assertEquals("103", result.getOrThrow().identifier)
        assertEquals(3, result.getOrThrow().numberOfUnits)
    }

    @Test
    fun `registerUnit sin conjunto retorna validacion sin llamar la API`() = runTest {
        val api = FakeResidentialApi()

        val result = ResidentialRepositoryImpl(api).registerUnit("")

        assertTrue(result.isFailure)
        assertTrue((result.exceptionOrNull() as AppException).error is AppError.Validation)
        assertEquals(0, api.createCalls)
    }

    @Test
    fun `registerUnit mapea http 403`() = runTest {
        val api = FakeResidentialApi(
            createError = httpException(403, "Acceso denegado")
        )

        val result = ResidentialRepositoryImpl(api).registerUnit("complex-1")

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.Forbidden)
        assertEquals("Acceso denegado", error.message)
    }

    @Test
    fun `registerUnit mapea http 404`() = runTest {
        val api = FakeResidentialApi(
            createError = httpException(404, "Conjunto no encontrado")
        )

        val result = ResidentialRepositoryImpl(api).registerUnit("complex-1")

        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.NotFound)
        assertEquals("Conjunto no encontrado", error.message)
    }

    @Test
    fun `registerUnit mapea error de red`() = runTest {
        val api = FakeResidentialApi(
            createError = IOException("sin red")
        )

        val result = ResidentialRepositoryImpl(api).registerUnit("complex-1")

        assertTrue(result.isFailure)
        assertTrue((result.exceptionOrNull() as AppException).error is AppError.NoConnection)
    }

    @Test
    fun `updateUnit envia conjunto unidad e identificador y mapea la respuesta`() = runTest {
        val api = FakeResidentialApi(
            updated = UpdateUnitResponseDto(id = "unit-1", identifier = "A-101")
        )

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        assertTrue(result.isSuccess)
        assertEquals("complex-1", api.lastUpdateComplexId)
        assertEquals("unit-1", api.lastUpdateUnitId)
        assertEquals(UpdateUnitRequestDto("A-101"), api.lastUpdateRequest)
        assertEquals("A-101", result.getOrThrow())
    }

    @Test
    fun `updateUnit mapea http 400`() = runTest {
        val api = FakeResidentialApi(
            updateError = httpException(400, "identifier no puede exceder 80 caracteres")
        )

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.Validation)
        assertEquals("identifier no puede exceder 80 caracteres", error.message)
    }

    @Test
    fun `updateUnit mapea http 403`() = runTest {
        val api = FakeResidentialApi(
            updateError = httpException(403, "Acceso denegado")
        )

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.Forbidden)
        assertEquals("Acceso denegado", error.message)
    }

    @Test
    fun `updateUnit mapea http 404`() = runTest {
        val api = FakeResidentialApi(
            updateError = httpException(404, "Unidad no encontrada")
        )

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.NotFound)
        assertEquals("Unidad no encontrada", error.message)
    }

    @Test
    fun `updateUnit mapea http 409 y conserva el mensaje del backend`() = runTest {
        val api = FakeResidentialApi(
            updateError = httpException(409, "La unidad ya existe en este conjunto")
        )

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.Conflict)
        assertEquals("La unidad ya existe en este conjunto", error.message)
    }

    @Test
    fun `updateUnit mapea error de red`() = runTest {
        val api = FakeResidentialApi(updateError = IOException("sin red"))

        val result = ResidentialRepositoryImpl(api)
            .updateUnit("complex-1", "unit-1", "A-101")

        assertTrue(result.isFailure)
        assertTrue((result.exceptionOrNull() as AppException).error is AppError.NoConnection)
    }

    @Test
    fun `registerOwnerTenant envia datos y mapea la respuesta`() = runTest {
        val api = FakeResidentialApi(
            createdResident = OwnerTenantResponseDto(
                id = "resident-1",
                unitId = "unit-1",
                unitIdentifier = "101",
                type = "OWNER",
                fullName = "Ana Pérez"
            )
        )
        val draft = OwnerTenantDraft(
            type = ResidentType.OWNER,
            nombres = "Ana",
            apellidos = "Pérez",
            documento = "123456789",
            email = "ana@example.com",
            telefono = "3001234567"
        )

        val result = ResidentialRepositoryImpl(api)
            .registerOwnerTenant("complex-1", "unit-1", draft)

        assertTrue(result.isSuccess)
        assertEquals("complex-1", api.lastResidentComplexId)
        assertEquals("unit-1", api.lastResidentUnitId)
        assertEquals(
            CreateOwnerTenantRequestDto(
                type = "OWNER",
                nombres = "Ana",
                apellidos = "Pérez",
                documento = "123456789",
                email = "ana@example.com",
                telefono = "3001234567"
            ),
            api.lastResidentRequest
        )
        assertEquals("Ana Pérez", result.getOrThrow().fullName)
        assertEquals(ResidentType.OWNER, result.getOrThrow().type)
    }

    @Test
    fun `registerOwnerTenant sin conjunto o unidad no llama la API`() = runTest {
        val api = FakeResidentialApi()
        val repository = ResidentialRepositoryImpl(api)
        val draft = OwnerTenantDraft(
            ResidentType.TENANT, "Ana", "Pérez", "123", "ana@example.com", "300"
        )

        val noComplex = repository.registerOwnerTenant("", "unit-1", draft)
        val noUnit = repository.registerOwnerTenant("complex-1", "", draft)

        assertTrue(noComplex.isFailure)
        assertTrue(noUnit.isFailure)
        assertEquals(0, api.residentCalls)
    }

    @Test
    fun `registerOwnerTenant mapea errores http y de red`() = runTest {
        val draft = OwnerTenantDraft(
            ResidentType.OWNER, "Ana", "Pérez", "123", "ana@example.com", "300"
        )
        val cases = listOf(
            400 to AppError.Validation::class.java,
            403 to AppError.Forbidden::class.java,
            404 to AppError.NotFound::class.java,
            409 to AppError.Conflict::class.java
        )

        cases.forEach { (code, expected) ->
            val api = FakeResidentialApi(
                residentError = httpException(code, "Error de prueba")
            )
            val result = ResidentialRepositoryImpl(api)
                .registerOwnerTenant("complex-1", "unit-1", draft)

            assertTrue(result.isFailure)
            assertTrue(expected.isInstance((result.exceptionOrNull() as AppException).error))
        }

        val networkApi = FakeResidentialApi(residentError = IOException("sin red"))
        val networkResult = ResidentialRepositoryImpl(networkApi)
            .registerOwnerTenant("complex-1", "unit-1", draft)
        assertTrue(
            (networkResult.exceptionOrNull() as AppException).error is AppError.NoConnection
        )
    }

    private fun httpException(code: Int, message: String): HttpException {
        val json = """{"status":$code,"error":"Error","message":"$message","path":"/api/complexes/c1/units"}"""
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}

private class FakeResidentialApi(
    private val units: List<ResidentialUnitDto> = emptyList(),
    private val created: CreateUnitResponseDto = CreateUnitResponseDto("unit-3", "103", 3),
    private val createError: Throwable? = null,
    private val updated: UpdateUnitResponseDto =
        UpdateUnitResponseDto("unit-1", "A-101"),
    private val updateError: Throwable? = null,
    private val createdResident: OwnerTenantResponseDto = OwnerTenantResponseDto(
        id = "resident-1",
        unitId = "unit-1",
        unitIdentifier = "101",
        type = "OWNER",
        fullName = "Ana Pérez"
    ),
    private val residentError: Throwable? = null
) : ResidentialApi {
    var calls = 0
    var lastComplexId: String? = null
    var createCalls = 0
    var lastCreateComplexId: String? = null
    var updateCalls = 0
    var lastUpdateComplexId: String? = null
    var lastUpdateUnitId: String? = null
    var lastUpdateRequest: UpdateUnitRequestDto? = null
    var residentCalls = 0
    var lastResidentComplexId: String? = null
    var lastResidentUnitId: String? = null
    var lastResidentRequest: CreateOwnerTenantRequestDto? = null

    override suspend fun getCensus(complexId: String): List<ResidentialUnitDto> {
        calls++
        lastComplexId = complexId
        return units
    }

    override suspend fun createUnit(complexId: String): CreateUnitResponseDto {
        createCalls++
        lastCreateComplexId = complexId
        createError?.let { throw it }
        return created
    }

    override suspend fun updateUnit(
        complexId: String,
        unitId: String,
        request: UpdateUnitRequestDto
    ): UpdateUnitResponseDto {
        updateCalls++
        lastUpdateComplexId = complexId
        lastUpdateUnitId = unitId
        lastUpdateRequest = request
        updateError?.let { throw it }
        return updated
    }

    override suspend fun createOwnerTenant(
        complexId: String,
        unitId: String,
        request: CreateOwnerTenantRequestDto
    ): OwnerTenantResponseDto {
        residentCalls++
        lastResidentComplexId = complexId
        lastResidentUnitId = unitId
        lastResidentRequest = request
        residentError?.let { throw it }
        return createdResident
    }
}
