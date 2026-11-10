package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.ui.components.ErrorBanner
import co.edu.unicauca.urbanguard.ui.components.ErrorView

@Composable
internal fun ChooseComplex(onSelectComplex: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.visitor_history_select_prompt))
        Button(onClick = onSelectComplex, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.visitor_history_select_complex))
        }
    }
}

@Composable
internal fun AccessError(error: AppError, onSessionExpired: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ErrorView(error = error)
        if (error is AppError.SessionExpired || error is AppError.ReauthRequired) {
            Button(onClick = onSessionExpired, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.visitor_history_sign_in))
            }
        }
    }
}

@Composable
internal fun HistoryProgress(label: Int, compact: Boolean = false) {
    val description = stringResource(label)
    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(if (compact) 24.dp else 40.dp).semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            }
        )
    }
}

@Composable
internal fun HistoryPageError(error: AppError, onAction: (HistoryAction) -> Unit) {
    val retry = { onAction(HistoryAction.Retry) }
    if (error is AppError.Validation) {
        Column {
            ErrorBanner(error.message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            OutlinedButton(onClick = retry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.visitor_history_restart_page))
            }
        }
    } else {
        val message = if (error is AppError.Timeout) stringResource(R.string.visitor_history_timeout) else error.message
        ErrorBanner(message, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }, onRetry = retry)
    }
}
