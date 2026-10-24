package co.edu.unicauca.urbanguard.data.remote.api.service

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import java.util.concurrent.atomic.AtomicReference

class PatrolApiContractTest {

    @Test
    fun `los endpoints de checkpoints usan el contrato de security-service`() {
        assertEquals(PatrolApiPaths.POINTS, pathOf(PatrolApi::class.java, "getPoints"))
        assertEquals(PatrolApiPaths.POINT, pathOf(PatrolApi::class.java, "getPoint"))
        assertEquals(PatrolApiPaths.POINTS, pathOf(PatrolApi::class.java, "createPoint"))
        assertEquals(PatrolApiPaths.POINT, pathOf(PatrolApi::class.java, "updatePoint"))
        assertEquals(
            PatrolApiPaths.TAG_ASSIGNMENTS,
            pathOf(PatrolApi::class.java, "replaceTag")
        )
    }

    @Test
    fun `los endpoints de rutas viven bajo security`() {
        assertEquals(PatrolApiPaths.ROUTES, pathOf(PatrolApi::class.java, "getRoutes"))
        assertEquals(PatrolApiPaths.ROUTES, pathOf(PatrolApi::class.java, "createRoute"))
        assertEquals(PatrolApiPaths.ROUTE, pathOf(PatrolApi::class.java, "updateRoute"))
        assertEquals(PatrolApiPaths.ROUTE, pathOf(PatrolApi::class.java, "deleteRoute"))
    }

    @Test
    fun `los endpoints de rondas y turnos viven bajo security`() {
        assertEquals(PatrolApiPaths.ROUNDS, pathOf(PatrolRoundApi::class.java, "getRounds"))
        assertEquals(PatrolApiPaths.ROUNDS, pathOf(PatrolRoundApi::class.java, "startRound"))
        assertEquals(PatrolApiPaths.ROUND_SCANS, pathOf(PatrolRoundApi::class.java, "registerScan"))
        assertEquals(PatrolApiPaths.ROUND_FINISH, pathOf(PatrolRoundApi::class.java, "finishRound"))
        assertEquals(PatrolApiPaths.SHIFTS, pathOf(PatrolShiftApi::class.java, "getShifts"))
        assertEquals(PatrolApiPaths.SHIFT_DETAIL, pathOf(PatrolShiftApi::class.java, "getShiftDetail"))
        assertEquals(PatrolApiPaths.GUARD_SUMMARY, pathOf(PatrolShiftApi::class.java, "getGuardSummary"))
    }

    @Test
    fun `getPoints con 200 y arreglo vacio devuelve lista vacia`() = runBlocking {
        val requestedPath = AtomicReference<String>()
        val api = retrofitReturning("[]", requestedPath).create(PatrolApi::class.java)

        val points = api.getPoints("complex-1")

        assertTrue(points.isEmpty())
        assertEquals(
            "/api/complexes/complex-1/checkpoints",
            requestedPath.get()
        )
    }

    @Test
    fun `getRoutes con 200 y arreglo vacio devuelve lista vacia`() = runBlocking {
        val requestedPath = AtomicReference<String>()
        val api = retrofitReturning("[]", requestedPath).create(PatrolApi::class.java)

        val routes = api.getRoutes("complex-1")

        assertTrue(routes.isEmpty())
        assertEquals(
            "/api/complexes/complex-1/patrol-routes",
            requestedPath.get()
        )
    }

    private fun retrofitReturning(
        body: String,
        requestedPath: AtomicReference<String>
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                requestedPath.set(request.url.encodedPath)
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun pathOf(type: Class<*>, methodName: String): String? {
        val method = type.declaredMethods.first { it.name == methodName }
        return method.getAnnotation(GET::class.java)?.value
            ?: method.getAnnotation(POST::class.java)?.value
            ?: method.getAnnotation(PUT::class.java)?.value
            ?: method.getAnnotation(PATCH::class.java)?.value
            ?: method.getAnnotation(DELETE::class.java)?.value
    }
}
