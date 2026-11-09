package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.domain.model.GuardMembership

interface GuardRepository {

    /**
     * Registra a [email] como vigilante de [complexId]. El teléfono es la
     * contraseña inicial de la cuenta.
     *
     * Es idempotente por email en la parte de Auth0: si el correo ya existía
     * porque el vigilante trabaja en otro conjunto, el backend solo lo agrega a
     * esta Organization y devuelve 201. El 409 es únicamente para el caso de que
     * ya sea personal de *este* conjunto.
     */
    suspend fun registerGuard(
        complexId: String,
        email: String,
        nombres: String,
        apellidos: String,
        telefono: String
    ): Result<CreatedGuard>

    suspend fun getGuards(complexId: String, status: String): Result<List<GuardMembership>>

    suspend fun setGuardActive(
        complexId: String,
        membershipId: String,
        active: Boolean
    ): Result<Unit>
}
