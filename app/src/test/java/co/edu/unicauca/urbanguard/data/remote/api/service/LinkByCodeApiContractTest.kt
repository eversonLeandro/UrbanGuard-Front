package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateLinkRequestRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicReference

class LinkByCodeApiContractTest {
    private val request = AtomicReference<Request>()

    private fun api(body: String): LinkByCodeApi {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val incoming = chain.request()
            request.set(incoming)
            Response.Builder().request(incoming).protocol(Protocol.HTTP_1_1).code(201)
                .message("Created")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        return Retrofit.Builder().baseUrl("https://example.test/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(LinkByCodeApi::class.java)
    }

    @Test
    fun `POST de vinculacion envia el codigo del conjunto`() = runBlocking {
        val api = api(
            """{"id":"r1","userId":"u1","complexId":"complex-1","unitId":"unit-1",""" +
                """"unit":"101","status":"PENDING","roleSyncPending":false}"""
        )

        api.requestLink(
            CreateLinkRequestRequestDto(
                complexId = "complex-1",
                unitId = "unit-1",
                codigo = "K7M3PX"
            )
        )

        assertEquals("POST", request.get().method)
        assertEquals("/api/community/solicitudes-vinculacion", request.get().url.encodedPath)
        val body = requireNotNull(request.get().body)
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        assertEquals(
            """{"complexId":"complex-1","unitId":"unit-1","codigo":"K7M3PX"}""",
            buffer.readUtf8()
        )
    }
}
