package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dayLabelFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault())
private val hourLabelFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm")

/**
 * Entrada de patrullaje del guardia: ronda de hoy, ronda en curso y rondas
 * recientes del conjunto.
 */
@Composable
fun GuardSeguridadScreen(
    onNotificationsClick: (() -> Unit)? = null,
    onActiveRound: () -> Unit = {},
    viewModel: GuardSeguridadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event == GuardSeguridadEvent.GoToActiveRound) onActiveRound()
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val errorMessage = uiState.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Rondas y Patrullaje",
            onNotificationsClick = onNotificationsClick
        )

        when {
            uiState.isLoading -> LoadingRounds()
            errorMessage != null && uiState.todayRoutes.isEmpty() ->
                RoundsError(
                    message = errorMessage,
                    onRetryClick = viewModel::onRefresh
                )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.activeRound?.let { round ->
                    item(key = "active") {
                        ActiveRoundBanner(
                            roundName = round.routeName,
                            onContinue = viewModel::onContinueRound
                        )
                    }
                }

                item(key = "today-title") {
                    SectionTitle(
                        if (uiState.todayRoutes.size == 1) "Ronda de hoy" else "Rondas de hoy"
                    )
                }

                item(key = "today-list") {
                    TodayRoutesSection(
                        routes = uiState.todayRoutes,
                        onStart = viewModel::onStartRoute
                    )
                }

                if (uiState.recentRounds.isNotEmpty()) {
                    item(key = "recent-title") {
                        SectionTitle("Mis rondas")
                    }
                    items(uiState.recentRounds, key = { it.id }) { round ->
                        RecentRoundRow(round = round)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = UgNavy
    )
}

@Composable
private fun ActiveRoundBanner(roundName: String, onContinue: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UgEmerald),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ronda en curso",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = roundName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            TextButton(onClick = onContinue) {
                Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TodayRoutesSection(
    routes: List<PatrolRoute>,
    onStart: (PatrolRoute) -> Unit
) {
    if (routes.isEmpty()) {
        Text(
            text = "Hoy no hay rondas programadas.",
            fontSize = 13.5.sp,
            color = UgTextSecondary
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        routes.forEach { route ->
            TodayRouteCard(route = route, onStart = { onStart(route) })
        }
    }
}

@Composable
private fun TodayRouteCard(route: PatrolRoute, onStart: () -> Unit) {
    val summary = todayScheduleSummary(route)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            UgNavyGradient,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = UgNavy
                    )
                    Text(
                        text = "Hoy ${summary.first} · ${route.pointIds.size} puntos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UgTextPrimary
                    )
                    Text(
                        text = summary.second,
                        fontSize = 12.sp,
                        color = UgTextSecondary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Iniciar ronda", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun RecentRoundRow(round: PatrolRound) {
    val status = round.status()
    val badgeColor = when (status) {
        RoundStatus.IN_PROGRESS -> UgNavy
        RoundStatus.COMPLETED -> UgEmerald
        RoundStatus.INCOMPLETE -> Color(0xFFF57C00)
    }
    val badgeText = when (status) {
        RoundStatus.IN_PROGRESS -> "EN CURSO"
        RoundStatus.COMPLETED -> "COMPLETADA"
        RoundStatus.INCOMPLETE -> "INCOMPLETA"
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = UgNavy,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = round.routeName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UgTextPrimary
                )
                Text(
                    text = roundHistoryLabel(round),
                    fontSize = 12.sp,
                    color = UgTextSecondary
                )
            }
            Text(
                text = badgeText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Horas de hoy ("10:00, 22:00") y conteo ("2 horarios hoy") de la ruta.
 * Una sola función para no superar el máximo por archivo de detekt.
 */
private fun todayScheduleSummary(route: PatrolRoute): Pair<String, String> {
    val day = LocalDate.now().dayOfWeek
    val times = route.schedules
        .filter { it.days.contains(day) }
        .map { it.time }
        .sorted()
    val label = times.joinToString(", ") { it.format(hourLabelFormat) }.ifBlank { "—" }
    val count = if (times.size == 1) "1 horario hoy" else "${times.size} horarios hoy"
    return label to count
}

private fun roundHistoryLabel(round: PatrolRound): String {    val date = dayLabelFormat.format(round.startedAt)
    val done = round.donePointIds().size
    return if (round.totalPoints > 0) {
        "$date · $done/${round.totalPoints} puntos"
    } else {
        "$date · ${round.scans.size} lecturas"
    }
}

@Composable
private fun LoadingRounds() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun RoundsError(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Reintentar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
