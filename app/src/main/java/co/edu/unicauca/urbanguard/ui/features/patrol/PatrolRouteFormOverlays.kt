package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.runtime.Composable

@Composable
internal fun PatrolRouteFormOverlays(
    uiState: PatrolRouteFormUiState,
    viewModel: PatrolRouteFormViewModel
) {
    uiState.routeChangeBlockedMessage?.let { message ->
        PatrolRouteChangeBlockedDialog(
            message = message,
            onDismiss = viewModel::onDismissRouteChangeBlocked
        )
    }

    val editingIndex = uiState.editingTimeIndex
    if (editingIndex != null) {
        val current = uiState.schedules.getOrNull(editingIndex)
        if (current != null) {
            RouteTimeDialog(
                hour = current.time.hour,
                minute = current.time.minute,
                onConfirm = { hour, minute ->
                    viewModel.onScheduleEvent(RouteScheduleEvent.PickTime(hour, minute))
                },
                onDismiss = {
                    viewModel.onScheduleEvent(RouteScheduleEvent.ClosePicker)
                }
            )
        }
    }
}
