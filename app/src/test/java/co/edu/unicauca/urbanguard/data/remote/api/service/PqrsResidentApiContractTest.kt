package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePqrsRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicReference

class PqrsResidentApiContractTest {
    private val request = AtomicReference<Request>()
    private fun api(body: String): PqrsResidentApi {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val incoming = chain.request()
            request.set(incoming)
            Response.Builder().request(incoming).protocol(Protocol.HTTP_1_1).code(200)
                .message("OK")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        return Retrofit.Builder().baseUrl("https://example.test/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(PqrsResidentApi::class.java)
    }

    @Test fun `POST envia contrato residente y llave de idempotencia`() = runBlocking {
        val api = api(
            """{"id":"p1","type":"PETITION","subject":"S","description":"D", """ +
                """"status":"SUBMITTED","createdAt":"2026-10-30T12:00:00Z"}"""
        )
        val result = api.create("complex-1", "key-1", CreatePqrsRequestDto("PETITION", "S", "D"))
        assertEquals("p1", result.id)
        assertEquals("POST", request.get().method)
        assertEquals("/api/complexes/complex-1/pqrs", request.get().url.encodedPath)
        assertEquals("key-1", request.get().header("Idempotency-Key"))
        val body = requireNotNull(request.get().body)
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        assertEquals("""{"type":"PETITION","subject":"S","description":"D"}""", buffer.readUtf8())
    }

    @Test fun `GET mine envia filtros opcionales y usa resumen sin descripcion`() = runBlocking {
        val api = api(
            """[{"id":"p1","type":"CLAIM","subject":"S", """ +
                """"status":"REJECTED","createdAt":"2026-10-30T12:00:00Z"}]"""
        )
        val result = api.getMine("complex-1", "REJECTED", "CLAIM")
        assertEquals("p1", result.single().id)
        assertEquals("/api/complexes/complex-1/pqrs/mine", request.get().url.encodedPath)
        assertEquals("REJECTED", request.get().url.queryParameter("status"))
        assertEquals("CLAIM", request.get().url.queryParameter("type"))
        assertFalse(result.single().toString().contains("description"))
    }

    @Test fun `GET detalle propio recibe historial y usa id en ruta`() = runBlocking {
        val api = api(
            """{"id":"p1","type":"PETITION","subject":"S","description":"D", """ +
                """"status":"CLOSED","createdAt":"2026-10-30T12:00:00Z","history":[""" +
                """{"id":"h1","fromStatus":"SUBMITTED","toStatus":"IN_PROGRESS",""" +
                """"response":"En revisión","createdAt":"2026-10-30T12:30:00Z"},""" +
                """{"id":"h2","fromStatus":"IN_PROGRESS","toStatus":"CLOSED",""" +
                """"response":"Solucionado","createdAt":"2026-10-30T13:00:00Z"}]}"""
        )
        val result = api.getOwnDetail("complex-1", "p1")
        assertEquals("D", result.description)
        assertEquals(2, result.history?.size)
        assertEquals("SUBMITTED", result.history?.first()?.fromStatus)
        assertEquals("CLOSED", result.history?.last()?.toStatus)
        assertEquals("Solucionado", result.history?.last()?.response)
        assertEquals("/api/complexes/complex-1/pqrs/p1", request.get().url.encodedPath)
        assertTrue(request.get().url.queryParameterNames.isEmpty())
    }

    @Test fun `GET detalle previo sin historial se puede leer`() = runBlocking {
        val api = api(
            """{"id":"p1","type":"PETITION","subject":"S","description":"D",""" +
                """"status":"SUBMITTED","createdAt":"2026-10-30T12:00:00Z"}"""
        )
        assertTrue(api.getOwnDetail("complex-1", "p1").history.isNullOrEmpty())
    }
}
