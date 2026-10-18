package co.edu.unicauca.urbanguard.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.notifications.GetUnreadNotificationsCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Alimenta la insignia de la campana. Comparte el [NotificationBadgeStore]
 * singleton, de modo que todas las pantallas muestran el mismo contador.
 */
@HiltViewModel
class NotificationBadgeViewModel @Inject constructor(
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase,
    private val store: NotificationBadgeStore
) : ViewModel() {

    val unreadCount: StateFlow<Int> = store.unreadCount

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            getUnreadNotificationsCountUseCase().onSuccess(store::set)
        }
    }
}
