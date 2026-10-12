package co.edu.unicauca.urbanguard.core.push

/**
 * Coordina el registro del token FCM contra el backend. Todo es best-effort y
 * silencioso: el push es informativo y jamás debe romper el login, el cambio
 * de conjunto ni el logout con un error visible.
 */
interface PushRegistrar {

    /**
     * Registra el token en el conjunto activo (si lo hay). Sin conjunto
     * activo no hay nada que registrar.
     */
    fun registerActiveConjunto()

    /**
     * Registra el token para un conjunto concreto aunque el usuario todavía no
     * sea miembro. Se invoca al enviar una solicitud de vinculación: el backend
     * lo guarda como token provisional y solo le enviará los eventos dirigidos a
     * él, para que reciba la decisión del administrador (aprobada o rechazada).
     */
    fun registerForConjunto(conjuntoId: String)

    /** Guarda el token rotado y lo registra si ya hay conjunto activo. */
    fun onTokenRotated(token: String)

    /**
     * Desactiva el token de la cuenta actual y luego ejecuta [onComplete]
     * (siempre, incluso si la red falla).
     */
    fun unregisterCurrent(onComplete: () -> Unit)
}
