package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.service.VisitorApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryPage
import com.google.gson.JsonParser
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VisitorHistoryRepositoryContractTest {
    private val request = AtomicReference<Request>()

    @Test
    fun `consulta GET con las cuatro combinaciones de fechas y conserva el contrato`() = runBlocking {
        val repository = repository()
        val from = LocalDate.of(2026, 11, 1)
        val to = LocalDate.of(2026, 11, 9)
        val datePairs = listOf(null to null, from to null, null to to, from to to)

        for ((start, end) in datePairs) {
            val result = repository.getVisitorHistory(COMPLEX, start, end, 2, 20).getOrThrow()
            val sent = request.get()
            assertEquals("GET", sent.method)
            assertEquals("/api/complexes/$COMPLEX/visitor-invitations/history", sent.url.encodedPath)
            assertEquals("2", sent.url.queryParameter("page"))
            assertEquals("20", sent.url.queryParameter("size"))
            assertEquals(start?.toString(), sent.url.queryParameter("from"))
            assertEquals(end?.toString(), sent.url.queryParameter("to"))
            val dates = listOfNotNull("from".takeIf { start != null }, "to".takeIf { end != null })
            assertEquals(setOf("page", "size") + dates, sent.url.queryParameterNames)
            assertEquals(
                VisitorHistoryPage(
                    items = listOf(
                        VisitorHistoryItem(INVITATION, "Usuario", "Carlos Gómez", 3, Instant.parse(ENTRY))
                    ),
                    page = 2,
                    size = 20,
                    totalElements = 41,
                    totalPages = 3
                ),
                result
            )
        }
    }

    @Test
    fun `distingue historial vacio y pagina vacia fuera del ultimo indice`() = runBlocking {
        val empty = repository(pageJson(content = "[]", page = 0, total = 0, pages = 0))
            .getVisitorHistory(COMPLEX, null, null, 0, 20).getOrThrow()
        assertEquals(VisitorHistoryPage(emptyList(), 0, 20, 0, 0), empty)

        val beyondLast = repository(pageJson(content = "[]", page = 3))
            .getVisitorHistory(COMPLEX, null, null, 3, 20).getOrThrow()
        assertEquals(VisitorHistoryPage(emptyList(), 3, 20, 41, 3), beyondLast)
    }

    @Test
    fun `un item incompleto o invalido falla en vez de mostrar un historial parcial`() = runBlocking {
        val missingFields = listOf("invitationId", "creatorName", "visitorName", "guestCount", "actualEntryAt")
            .map { field ->
                val item = JsonParser.parseString(ITEM).asJsonObject
                item.remove(field)
                item.toString()
            }
        val invalidItems = missingFields + listOf(
            "null",
            ITEM.replace(INVITATION, "not-a-uuid"),
            ITEM.replace(INVITATION, "1-1-1-1-1"),
            ITEM.replace("Usuario", " "),
            ITEM.replace("Carlos Gómez", ""),
            ITEM.replace("\"guestCount\": 3", "\"guestCount\": 0"),
            ITEM.replace(ENTRY, "2026-11-09")
        )
        for (item in invalidItems) {
            val result = repository(pageJson(content = "[$item]")).getVisitorHistory(COMPLEX, null, null, 2, 20)
            val error = AppError.from(requireNotNull(result.exceptionOrNull()))
            assertTrue("Se esperaba error al leer $item", error is AppError.Unexpected)
        }
    }

    @Test
    fun `rechaza metadatos omitidos o incoherentes con la pagina solicitada`() = runBlocking {
        val missingFields = listOf("content", "page", "size", "totalElements", "totalPages")
            .map { field ->
                val response = JsonParser.parseString(pageJson()).asJsonObject
                response.remove(field)
                response.toString()
            }
        val invalidPages = missingFields + listOf(
            pageJson(page = 1),
            pageJson(size = 0),
            pageJson(size = 21),
            pageJson(total = -1),
            pageJson(pages = 4),
            pageJson(content = "[]"),
            pageJson(content = "[$ITEM,$ITEM]")
        )
        for (payload in invalidPages) {
            val result = repository(payload).getVisitorHistory(COMPLEX, null, null, 2, 20)
            val error = AppError.from(requireNotNull(result.exceptionOrNull()))
            assertTrue("Se esperaba error al leer $payload", error is AppError.Unexpected)
        }
    }

    @Test
    fun `mantiene los errores HTTP de dominio`() = runBlocking {
        val errors = mapOf(
            400 to AppError.Validation::class.java,
            401 to AppError.SessionExpired::class.java,
            403 to AppError.Forbidden::class.java,
            404 to AppError.NotFound::class.java,
            500 to AppError.ServerError::class.java
        )
        for ((code, type) in errors) {
            val failure = repository("{}", code).getVisitorHistory(COMPLEX, null, null, 0, 20).exceptionOrNull()
            assertEquals("HTTP $code", type, AppError.from(requireNotNull(failure)).javaClass)
        }
    }

    @Test
    fun `mantiene un timeout como error recuperable`() = runBlocking {
        val failure = repository(failure = SocketTimeoutException("timeout"))
            .getVisitorHistory(COMPLEX, null, null, 0, 20).exceptionOrNull()
        assertTrue(AppError.from(requireNotNull(failure)) is AppError.Timeout)
    }

    @Test
    fun `conjunto vacio no hace peticion`() = runBlocking {
        val failure = repository().getVisitorHistory(" ", null, null, 0, 20).exceptionOrNull()
        assertTrue(AppError.from(requireNotNull(failure)) is AppError.Validation)
        assertNull(request.get())
    }

    @Test
    fun `JSON malformado se reporta como error de respuesta`() = runBlocking {
        for (body in listOf("{\"content\":[", "{\"content\":]}")) {
            val result = repository(body).getVisitorHistory(COMPLEX, null, null, 0, 20)
            val error = AppError.from(requireNotNull(result.exceptionOrNull()))
            assertEquals(AppError.Unexpected("No se pudo leer la respuesta del historial de visitantes."), error)
        }
    }

    private fun repository(
        body: String = pageJson(),
        code: Int = 200,
        failure: IOException? = null
    ): VisitorRepositoryImpl {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val incoming = chain.request()
            request.set(incoming)
            failure?.let { throw it }
            Response.Builder()
                .request(incoming)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("HTTP $code")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://example.test/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(VisitorApi::class.java)
        return VisitorRepositoryImpl(api)
    }

    private fun pageJson(content: String = "[$ITEM]", page: Int = 2, size: Int = 20, total: Long = 41, pages: Int = 3) =
        """{"content": $content, "page": $page, "size": $size, "totalElements": $total, "totalPages": $pages}"""

    private companion object {
        const val COMPLEX = "10000000-0000-0000-0000-000000000001"
        const val INVITATION = "10000000-0000-0000-0000-000000000002"
        const val ENTRY = "2026-11-09T19:35:00Z"
        const val ITEM = """{
            "invitationId": "$INVITATION",
            "creatorName": "Usuario",
            "visitorName": "Carlos Gómez",
            "guestCount": 3,
            "actualEntryAt": "$ENTRY"
        }"""
    }
}
