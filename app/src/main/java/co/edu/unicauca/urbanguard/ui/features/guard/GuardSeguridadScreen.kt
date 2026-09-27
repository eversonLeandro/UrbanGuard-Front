package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgEmeraldLight
import co.edu.unicauca.urbanguard.ui.theme.UgInfoBg
import co.edu.unicauca.urbanguard.ui.theme.UgInfoIcon
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Pantalla de rondas y patrullaje.
 *
 * La interfaz actual es un mock estatico: [GuardSeguridadViewModel] todavia no
 * carga datos, asi que aqui no se observa su `uiState`. El parametro se conserva
 * a proposito para que la pantalla pueda engancharse al ViewModel cuando la
 * feature se implemente.
 *
 * [LongMethod] tambien esta suprimido: partir este composable en
 * subcomposables ahora es trabajo que se pierde al implementar la feature real.
 */
@Suppress("UnusedParameter", "LongMethod")
@Composable
fun GuardSeguridadScreen(
    onNotificationsClick: (() -> Unit)? = null,
    viewModel: GuardSeguridadViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Rondas y Patrullaje",
            onNotificationsClick = onNotificationsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Tarjeta de Estado del Patrullaje ─────────────────────
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(UgNavyGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalPolice,
                                contentDescription = null,
                                tint = UgEmeraldLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Ruta Programada", fontSize = 12.sp, color = UgTextSecondary)
                            Text(
                                "Patrulla Perimetral Norte",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = UgNavy
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(UgInfoBg)
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Hora: 11:30 AM",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UgInfoIcon
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = UgBorderSoft)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("4 Puntos de Control NFC", fontSize = 12.5.sp, color = UgTextSecondary)
                        Text(
                            "Duración estimada: 25 min",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = UgNavy
                        )
                    }

                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = UgEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Iniciar Ronda de Patrullaje",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Puntos de Control de la Ronda ──────────────────────
            Text(
                "Puntos de Verificación de Ruta",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PatrolCheckpointItem(
                        number = "1",
                        name = "Estacionamiento Visitantes Norte",
                        isDone = true,
                        time = "09:05 AM"
                    )
                    HorizontalDivider(color = UgBorderSoft)
                    PatrolCheckpointItem(
                        number = "2",
                        name = "Piscina y Zonas Húmedas",
                        isDone = true,
                        time = "09:14 AM"
                    )
                    HorizontalDivider(color = UgBorderSoft)
                    PatrolCheckpointItem(
                        number = "3",
                        name = "Puerta Peatonal Auxiliar Sur",
                        isDone = false,
                        time = "Pendiente"
                    )
                    HorizontalDivider(color = UgBorderSoft)
                    PatrolCheckpointItem(
                        number = "4",
                        name = "Subestación Eléctrica y Cuarto de Bombas",
                        isDone = false,
                        time = "Pendiente"
                    )
                }
            }
        }
    }
}

@Composable
private fun PatrolCheckpointItem(
    number: String,
    name: String,
    isDone: Boolean,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDone) UgSuccessBg else UgInputBg),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = UgSuccessText,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(number, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = UgNavy)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = UgNavy)
            Text("Punto de escaneo QR / NFC", fontSize = 11.5.sp, color = UgTextSecondary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDone) UgSuccessBg else UgInputBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isDone) "Completado: $time" else "Estado: $time",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) UgSuccessText else UgTextSecondary
                )
            }
        }
    }
}
