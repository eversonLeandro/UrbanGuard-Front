package co.edu.unicauca.urbanguard.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import co.edu.unicauca.urbanguard.core.media.ProfilePhotoPreparer
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.ProfileStateHolder
import co.edu.unicauca.urbanguard.domain.repository.UserRepository
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import co.edu.unicauca.urbanguard.domain.usecase.users.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
// Un mismo ViewModel mantiene los flujos aprobados de perfil y contraseña.
@Suppress("LongParameterList", "TooManyFunctions")
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val reauthLoginProvider: ReauthLoginProvider,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val profileState: ProfileStateHolder,
    private val users: UserRepository,
    private val photoPreparer: ProfilePhotoPreparer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                _uiState.update {
                    it.copy(
                        userName = session?.name ?: "",
                        userRole = when (session?.role) {
                            UserRole.ADMIN -> "ADMIN"
                            UserRole.GUARD -> "GUARDIA"
                            UserRole.RESIDENT -> "RESIDENT"
                            UserRole.USER -> "USER"
                            UserRole.SUPERADMIN -> "SUPERADMIN"
                            null -> ""
                        },
                        role = session?.role,
                        profilePhotoUrl = profileState.currentProfile()?.profilePhotoUrl,
                        profilePhotoExpiresAt = profileState.currentProfile()?.profilePhotoExpiresAt
                    )
                }
                if (session != null) profileState.ensureFresh()
            }
        }
        viewModelScope.launch {
            profileState.profile.collect {
                val profile = profileState.currentProfile()
                _uiState.update {
                    it.copy(
                        profilePhotoUrl = profile?.profilePhotoUrl,
                        profilePhotoExpiresAt = profile?.profilePhotoExpiresAt,
                        userName = sessionManager.session.value?.name?.takeIf(String::isNotBlank)
                            ?: profile?.name.orEmpty()
                    )
                }
            }
        }
    }

    fun ensureFresh() { viewModelScope.launch { profileState.ensureFresh() } }

    fun onImageError(url: String) {
        viewModelScope.launch { profileState.refreshAfterImageError(url) }
    }

    fun onPhotoErrorDismissed() { _uiState.update { it.copy(photoError = null) } }

    fun onCameraDenied() {
        _uiState.update { it.copy(photoError =
            "Permite el uso de la cámara para tomar una foto. " +
                "Puedes elegir una imagen de la galería.") }
    }

    fun onCameraFailure() {
        _uiState.update { it.copy(photoError =
            "No pudimos abrir la cámara. Inténtalo de nuevo o elige una imagen de la galería.") }
    }

    fun createCameraOutput(): Uri = photoPreparer.cameraOutput().first
    fun discardCameraOutput(uri: Uri?) = photoPreparer.discardCameraOutput(uri)

    fun updatePhoto(uri: Uri, cameraSource: Boolean = false) {
        if (_uiState.value.isPhotoUploading) return
        val expectedUserId = sessionManager.session.value?.userId ?: return
        val uploadToken = sessionManager.token ?: return
        _uiState.update { it.copy(isPhotoUploading = true, photoError = null) }
        viewModelScope.launch {
            try {
                val prepared = photoPreparer.prepare(uri)
                prepared.fold(
                    onSuccess = photoReady@{ photo ->
                        try {
                            if (sessionManager.session.value?.userId != expectedUserId ||
                                sessionManager.token != uploadToken) return@photoReady
                            val result = users.updateProfilePhoto(photo.part, uploadToken)
                            if (sessionManager.session.value?.userId != expectedUserId) return@photoReady
                            result.fold(
                            onSuccess = { profileState.publish(it, expectedUserId) },
                            onFailure = { throwable ->
                                val error = (throwable as? AppException)?.error
                                    ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                                if (error is AppError.Timeout || error is AppError.Conflict) {
                                    val refreshed = profileState.ensureFresh(force = true)
                                    _uiState.update { state -> state.copy(photoError =
                                        if (refreshed.isSuccess) UNCERTAIN_MESSAGE else UNCERTAIN_REFRESH_FAILED) }
                                } else {
                                    _uiState.update { state -> state.copy(photoError = photoMessage(error)) }
                                }
                            }
                            )
                        } finally {
                            photo.cleanup()
                        }
                    },
                    onFailure = { throwable ->
                        val error = (throwable as? AppException)?.error
                            ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                        _uiState.update { it.copy(photoError = photoMessage(error)) }
                    }
                )
            } finally {
                _uiState.update { it.copy(isPhotoUploading = false) }
                if (cameraSource) photoPreparer.discardCameraOutput(uri)
            }
        }
    }

    private fun photoMessage(error: AppError): String = when (error) {
        is AppError.ImageTooLarge, is AppError.UnsupportedImage,
        is AppError.Validation, is AppError.Forbidden,
        is AppError.SessionExpired -> error.message
        is AppError.NoConnection -> "No pudimos actualizar la foto. Revisa tu conexión e inténtalo nuevamente."
        else -> "No pudimos actualizar la foto en este momento."
    }

    private companion object {
        const val UNCERTAIN_MESSAGE =
            "No pudimos confirmar si la foto se actualizó. Mostramos el estado actual de tu perfil."
        const val UNCERTAIN_REFRESH_FAILED =
            "No pudimos confirmar si la foto se actualizó ni consultar el estado actual. " +
                "Inténtalo de nuevo manualmente."
    }

    /**
     * Solicita al backend un ticket de cambio de contraseña.
     *
     * Flujo normal:
     *  1. Muestra loading.
     *  2. Llama al backend → recibe la URL del ticket.
     *  3. Escribe [ticketUrl]; la UI lo consume abriendo el Custom Tab.
     *
     * Flujo REAUTH_REQUIRED:
     *  1. El backend responde 401 REAUTH_REQUIRED (auth_time > 5 min).
     *  2. Se activa [needsReauth]; la UI muestra el diálogo de reautenticación.
     *  3. El usuario proporciona su contraseña via [onReauthSubmit].
     *  4. Tras re-login exitoso se reintenta automáticamente.
     */
    fun requestPasswordChange() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingTicket = true, passwordChangeError = null) }
            val result = changePasswordUseCase()
            result.fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(isRequestingTicket = false, ticketUrl = url)
                    }
                },
                onFailure = { throwable ->
                    val error = (throwable as? AppException)?.error
                        ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                    when (error) {
                        is AppError.ReauthRequired -> _uiState.update {
                            it.copy(isRequestingTicket = false, needsReauth = true)
                        }
                        else -> _uiState.update {
                            it.copy(isRequestingTicket = false, passwordChangeError = error.message)
                        }
                    }
                }
            )
        }
    }

    /**
     * Llamado desde la UI cuando el usuario confirma su email y contraseña en el diálogo
     * de reautenticación. Realiza login interactivo para renovar [auth_time] y reintenta
     * automáticamente la solicitud del ticket.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onReauthSubmit(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingTicket = true, needsReauth = false, passwordChangeError = null) }
            try {
                val newSession = reauthLoginProvider.login(email, password)
                sessionManager.saveSession(newSession)
                // Reintentar la solicitud del ticket con el token renovado
                val result = changePasswordUseCase()
                result.fold(
                    onSuccess = { url ->
                        _uiState.update { it.copy(isRequestingTicket = false, ticketUrl = url) }
                    },
                    onFailure = { throwable ->
                        val error = (throwable as? AppException)?.error
                            ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                        _uiState.update {
                            it.copy(isRequestingTicket = false, passwordChangeError = error.message)
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRequestingTicket = false,
                        passwordChangeError = (e as? AppException)?.error?.message
                            ?: "Contraseña incorrecta. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    /** Llamado por la UI una vez que consume [ticketUrl] abriendo el Custom Tab. */
    fun onTicketConsumed() {
        _uiState.update { it.copy(ticketUrl = null) }
    }

    /** Descarta el error visible al usuario. */
    fun onPasswordChangeErrorDismissed() {
        _uiState.update { it.copy(passwordChangeError = null) }
    }

    /** Cancela el diálogo de reautenticación sin reintentar. */
    fun onReauthDismissed() {
        _uiState.update { it.copy(needsReauth = false) }
    }
}
