package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePqrsRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsCreatedDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsDetailDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsHistoryEntryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ResidentPqrsSummaryDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PqrsResidentApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResidentPqrsRepositoryImplTest {
    @Test fun `create mapea respuesta residente y propaga contexto`() = runBlocking {
        val api = FakeApi()
        val result = ResidentPqrsRepositoryImpl(api).create(
            "complex-1", "key-1", ResidentPqrsDraft(PqrsType.PETITION, "S", "D")
        ).getOrThrow()
        assertEquals("key-1", api.key)
        assertEquals("complex-1", api.complexId)
        assertEquals("PETITION", api.body?.type)
        assertEquals(PqrsStatus.SUBMITTED, result.status)
        assertEquals("D", result.description)
    }

    @Test fun `listado propaga filtros y mapea rechazado`() = runBlocking {
        val api = FakeApi()
        val result = ResidentPqrsRepositoryImpl(api).getMine(
            "complex-1", PqrsStatus.REJECTED, PqrsType.CLAIM
        ).getOrThrow()
        assertEquals("REJECTED", api.status)
        assertEquals("CLAIM", api.type)
        assertEquals(PqrsStatus.REJECTED, result.single().status)
        assertEquals(PqrsType.CLAIM, result.single().type)
    }

    @Test fun `detalle propio mapea estados y comentarios del historial`() = runBlocking {
        val api = FakeApi()
        val result = ResidentPqrsRepositoryImpl(api).getOwnDetail("complex-1", "p1").getOrThrow()
        assertEquals("p1", api.pqrsId)
        assertEquals(PqrsStatus.CLOSED, result.status)
        assertEquals(1, result.history.size)
        assertEquals("h1", result.history.single().id)
        assertEquals(PqrsStatus.IN_PROGRESS, result.history.single().fromStatus)
        assertEquals(PqrsStatus.CLOSED, result.history.single().toStatus)
        assertEquals("Se completó la gestión", result.history.single().response)
        assertEquals("2026-10-30T13:00:00Z", result.history.single().createdAt)
    }

    @Test fun `detalle con historial vacio mantiene lista vacia`() = runBlocking {
        val api = FakeApi().apply { emptyHistory = true }
        val result = ResidentPqrsRepositoryImpl(api).getOwnDetail("complex-1", "p1").getOrThrow()
        assertTrue(result.history.isEmpty())
    }

    @Test fun `respuesta sin campo historial falla en vez de mostrar vacio falso`() = runBlocking {
        val api = FakeApi().apply { omitHistory = true }
        assertTrue(ResidentPqrsRepositoryImpl(api).getOwnDetail("complex-1", "p1").isFailure)
    }

    @Test fun `estado desconocido en historial falla sin mostrar uno incorrecto`() = runBlocking {
        val api = FakeApi().apply { invalidHistoryStatus = true }
        assertTrue(ResidentPqrsRepositoryImpl(api).getOwnDetail("complex-1", "p1").isFailure)
    }

    @Test fun `contexto vacio evita llamadas al api`() = runBlocking {
        val api = FakeApi()
        val repo = ResidentPqrsRepositoryImpl(api)
        assertTrue(repo.create("", "key", ResidentPqrsDraft(PqrsType.CLAIM, "S", "D")).isFailure)
        assertTrue(repo.create("c", "", ResidentPqrsDraft(PqrsType.CLAIM, "S", "D")).isFailure)
        assertTrue(repo.getMine("", null, null).isFailure)
        assertTrue(repo.getOwnDetail("c", "").isFailure)
        assertEquals(0, api.calls)
    }

    @Test fun `respuesta con estado desconocido falla sin degradar a radicada`() = runBlocking {
        val api = FakeApi().apply { invalidStatus = true }
        val result = ResidentPqrsRepositoryImpl(api).getMine("c", null, null)
        assertTrue(result.isFailure)
    }

    private class FakeApi : PqrsResidentApi {
        var calls = 0
        var complexId: String? = null
        var key: String? = null
        var body: CreatePqrsRequestDto? = null
        var status: String? = null
        var type: String? = null
        var pqrsId: String? = null
        var invalidStatus = false
        var invalidHistoryStatus = false
        var omitHistory = false
        var emptyHistory = false
        override suspend fun create(
            complexId: String,
            idempotencyKey: String,
            request: CreatePqrsRequestDto
        ): ResidentPqrsCreatedDto {
            calls++
            this.complexId = complexId
            key = idempotencyKey
            body = request
            return ResidentPqrsCreatedDto("p1", "PETITION", "S", "D", "SUBMITTED", "2026-10-30T12:00:00Z")
        }
        override suspend fun getMine(complexId: String, status: String?, type: String?): List<ResidentPqrsSummaryDto> {
            calls++
            this.status = status
            this.type = type
            return listOf(
                ResidentPqrsSummaryDto(
                    "p1", "CLAIM", "S", if (invalidStatus) "UNKNOWN" else "REJECTED",
                    "2026-10-30T12:00:00Z"
                )
            )
        }
        override suspend fun getOwnDetail(complexId: String, pqrsId: String): ResidentPqrsDetailDto {
            calls++
            this.pqrsId = pqrsId
            return ResidentPqrsDetailDto(
                "p1", "PETITION", "S", "D", "CLOSED", "2026-10-30T12:00:00Z",
                if (omitHistory) null else if (emptyHistory) emptyList() else listOf(ResidentPqrsHistoryEntryDto(
                    "h1", if (invalidHistoryStatus) "UNKNOWN" else "IN_PROGRESS",
                    "CLOSED", "Se completó la gestión", "2026-10-30T13:00:00Z"
                ))
            )
        }
    }
}
