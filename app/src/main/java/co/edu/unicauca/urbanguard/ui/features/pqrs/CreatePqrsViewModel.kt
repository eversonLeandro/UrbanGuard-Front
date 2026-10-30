package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.CreatePqrsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions") // Callbacks de formulario, validación e intento persistido en un único estado.
class CreatePqrsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val createPqrs: CreatePqrsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        if (savedStateHandle.get<String>(FORM_USER) == null ||
            savedStateHandle.get<String>(FORM_USER) == sessionManager.session.value?.userId
        ) restoredState() else CreatePqrsUiState()
    )
    val uiState: StateFlow<CreatePqrsUiState> = _uiState.asStateFlow()
    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val createdIds: Flow<String> = eventChannel.receiveAsFlow()

    init {
        val currentUser = sessionManager.session.value?.userId
        val owner = savedStateHandle.get<String>(FORM_USER)
        if (owner != null && currentUser != null && owner != currentUser) clearForm()
        if (currentUser != null) savedStateHandle[FORM_USER] = currentUser
        var hadActiveSession = currentUser != null
        viewModelScope.launch {
            sessionManager.session.map { it?.userId }.distinctUntilChanged().collect { userId ->
                val recordedOwner = savedStateHandle.get<String>(FORM_USER)
                when {
                    userId == null && hadActiveSession -> clearForm()
                    userId != null && recordedOwner != null && recordedOwner != userId -> clearForm()
                    userId != null && recordedOwner == userId &&
                        _uiState.value == CreatePqrsUiState() -> _uiState.value = restoredState()
                }
                if (userId != null) savedStateHandle[FORM_USER] = userId
                hadActiveSession = userId != null
            }
        }
    }

    fun onTypeChanged(type: PqrsType) {
        if (_uiState.value.isSubmitting || _uiState.value.completedElsewhere) return
        savedStateHandle[TYPE] = type.name
        _uiState.value = _uiState.value.copy(type = type, typeError = null, submitError = null)
    }

    fun onSubjectChanged(subject: String) {
        if (_uiState.value.isSubmitting || _uiState.value.completedElsewhere) return
        savedStateHandle[SUBJECT] = subject
        _uiState.value = _uiState.value.copy(subject = subject, subjectError = null, submitError = null)
    }

    fun onDescriptionChanged(description: String) {
        if (_uiState.value.isSubmitting || _uiState.value.completedElsewhere) return
        savedStateHandle[DESCRIPTION] = description
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = null,
            submitError = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.completedElsewhere) return
        val draft = validatedDraft(state) ?: return
        val session = sessionManager.session.value
        if (session?.role != UserRole.RESIDENT) {
            _uiState.value = state.copy(submitError = AppError.Forbidden.MESSAGE)
            return
        }
        val complexId = session.conjuntoActivoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(submitError = "No tienes un conjunto activo seleccionado.")
            return
        }
        val previous = savedAttempt()
        if (previous != null && previous.isDifferentFrom(session.userId, complexId, draft)) {
            _uiState.value = state.copy(
                requiresNewAttempt = true,
                submitError = "Cambiaste la solicitud. Revisa tus datos antes de iniciar un nuevo intento."
            )
            return
        }
        if (state.requiresNewAttempt) return
        val attempt = previous ?: PqrsAttempt(
            UUID.randomUUID().toString(), session.userId, complexId, draft
        ).also(::saveAttempt)
        _uiState.value = state.copy(isSubmitting = true, submitError = null)
        performCreate(attempt)
    }

    private fun performCreate(attempt: PqrsAttempt) {
        viewModelScope.launch {
            createPqrs(attempt.complexId, attempt.key, attempt.draft).fold(
                onSuccess = { created ->
                    if (isCurrentAttempt(attempt)) {
                        clearAttempt()
                        val current = sessionManager.session.value
                        val sameContext = attempt.matches(current)
                        savedStateHandle[COMPLETED_ELSEWHERE] = !sameContext
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            completedElsewhere = !sameContext,
                            submitError = if (sameContext) null else COMPLETED_ELSEWHERE_MESSAGE
                        )
                        if (sameContext) eventChannel.send(created.id)
                    }
                },
                onFailure = { failure ->
                    if (isCurrentAttempt(attempt)) {
                        val error = AppError.from(failure)
                        val conflict = error is AppError.Conflict
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            requiresNewAttempt = conflict,
                            submitError = if (conflict) CONFLICT_MESSAGE else error.message
                        )
                    }
                }
            )
        }
    }

    private fun isCurrentAttempt(attempt: PqrsAttempt): Boolean =
        savedStateHandle.get<String>(ATTEMPT_KEY) == attempt.key &&
            savedStateHandle.get<String>(ATTEMPT_USER) == attempt.userId &&
            sessionManager.session.value?.userId == attempt.userId

    private fun validatedDraft(state: CreatePqrsUiState): ResidentPqrsDraft? {
        val typeError = if (state.type == null) "Selecciona un tipo de PQRS." else null
        val subject = state.subject.trim()
        val subjectError = when {
            subject.isBlank() -> "Escribe el asunto."
            subject.length > SUBJECT_MAX -> "El asunto no puede superar 120 caracteres."
            else -> null
        }
        val description = state.description.trim()
        val descriptionError = when {
            description.isBlank() -> "Escribe la descripción."
            description.length > DESCRIPTION_MAX -> "La descripción no puede superar 2000 caracteres."
            else -> null
        }
        if (typeError != null || subjectError != null || descriptionError != null) {
            _uiState.value = state.copy(
                typeError = typeError,
                subjectError = subjectError,
                descriptionError = descriptionError
            )
            return null
        }
        return ResidentPqrsDraft(requireNotNull(state.type), subject, description)
    }

    fun startNewAttempt() {
        if (_uiState.value.isSubmitting || _uiState.value.completedElsewhere) return
        clearAttempt()
        _uiState.value = _uiState.value.copy(requiresNewAttempt = false, submitError = null)
        submit()
    }

    private fun savedAttempt(): PqrsAttempt? {
        val key = savedStateHandle.get<String>(ATTEMPT_KEY) ?: return null
        val userId = savedStateHandle.get<String>(ATTEMPT_USER) ?: return null
        val complexId = savedStateHandle.get<String>(ATTEMPT_COMPLEX) ?: return null
        val type = savedStateHandle.get<String>(ATTEMPT_TYPE)
            ?.let { runCatching { PqrsType.valueOf(it) }.getOrNull() } ?: return null
        return PqrsAttempt(
            key,
            userId,
            complexId,
            ResidentPqrsDraft(
                type,
                savedStateHandle[ATTEMPT_SUBJECT] ?: return null,
                savedStateHandle[ATTEMPT_DESCRIPTION] ?: return null
            )
        )
    }

    private fun saveAttempt(attempt: PqrsAttempt) {
        savedStateHandle[ATTEMPT_KEY] = attempt.key
        savedStateHandle[ATTEMPT_USER] = attempt.userId
        savedStateHandle[ATTEMPT_COMPLEX] = attempt.complexId
        savedStateHandle[ATTEMPT_TYPE] = attempt.draft.type.name
        savedStateHandle[ATTEMPT_SUBJECT] = attempt.draft.subject
        savedStateHandle[ATTEMPT_DESCRIPTION] = attempt.draft.description
    }

    private fun clearAttempt() {
        listOf(
            ATTEMPT_KEY, ATTEMPT_USER, ATTEMPT_COMPLEX, ATTEMPT_TYPE,
            ATTEMPT_SUBJECT, ATTEMPT_DESCRIPTION
        )
            .forEach { savedStateHandle.remove<String>(it) }
    }

    private fun clearForm() {
        clearAttempt()
        listOf(TYPE, SUBJECT, DESCRIPTION, COMPLETED_ELSEWHERE, FORM_USER)
            .forEach { savedStateHandle.remove<Any>(it) }
        _uiState.value = CreatePqrsUiState()
    }

    private fun restoredState() = CreatePqrsUiState(
        type = savedStateHandle.get<String>(TYPE)
            ?.let { runCatching { PqrsType.valueOf(it) }.getOrNull() },
        subject = savedStateHandle[SUBJECT] ?: "",
        description = savedStateHandle[DESCRIPTION] ?: "",
        completedElsewhere = savedStateHandle[COMPLETED_ELSEWHERE] ?: false
    )

    private data class PqrsAttempt(
        val key: String,
        val userId: String,
        val complexId: String,
        val draft: ResidentPqrsDraft
    ) {
        fun matches(current: UserSession?): Boolean = current?.userId == userId &&
            current.conjuntoActivoId == complexId && current.role == UserRole.RESIDENT

        fun isDifferentFrom(
            currentUserId: String,
            currentComplexId: String,
            currentDraft: ResidentPqrsDraft
        ): Boolean = userId != currentUserId || complexId != currentComplexId || draft != currentDraft
    }

    companion object {
        const val SUBJECT_MAX = 120
        const val DESCRIPTION_MAX = 2000
        const val CONFLICT_MESSAGE =
            "Esta solicitud cambió durante un reintento. Revisa tus datos antes de enviarla nuevamente."
        const val COMPLETED_ELSEWHERE_MESSAGE =
            "La PQRS fue radicada. Vuelve a Mis PQRS para consultarla en el conjunto correspondiente."
        private const val TYPE = "resident_pqrs_type"
        private const val SUBJECT = "resident_pqrs_subject"
        private const val DESCRIPTION = "resident_pqrs_description"
        private const val FORM_USER = "resident_pqrs_form_user"
        private const val COMPLETED_ELSEWHERE = "resident_pqrs_completed_elsewhere"
        private const val ATTEMPT_KEY = "resident_pqrs_attempt_key"
        private const val ATTEMPT_USER = "resident_pqrs_attempt_user"
        private const val ATTEMPT_COMPLEX = "resident_pqrs_attempt_complex"
        private const val ATTEMPT_TYPE = "resident_pqrs_attempt_type"
        private const val ATTEMPT_SUBJECT = "resident_pqrs_attempt_subject"
        private const val ATTEMPT_DESCRIPTION = "resident_pqrs_attempt_description"
    }
}

