package co.edu.unicauca.urbanguard.core.network

import kotlinx.coroutines.CancellationException

/**
 * Ejecuta una llamada de la capa de datos y devuelve su resultado como [Result].
 *
 * El `catch` amplio es deliberado y no un descuido: este es el limite del
 * repositorio, y aqui cualquier excepcion se traduce a un [AppException] de
 * dominio con su mensaje en español en vez de escapar al ViewModel. No se puede
 * enumerar una lista de excepciones concretas porque cada implementacion de API
 * lanza las suyas.
 *
 * [CancellationException] se relanza antes del `catch` amplio a proposito. En
 * Kotlin hereda de `IllegalStateException` y por tanto de `Exception`, asi que
 * sin esta guarda una corrutina cancelada se convertiria en un
 * `Result.failure` de dominio: el usuario veria un error de red que no existe y
 * la cancelacion se propagaria como si fuera un fallo de la API. Es el motivo de
 * que la regla TooGenericExceptionCaught este silenciada aqui y no en los 14
 * metodos que antes repetian este mismo bloque.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(NetworkErrorMapper.toAppException(e))
    }
