package co.edu.unicauca.urbanguard.ui.features.support

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private const val SUPPORT_EMAIL = "beja@gmail.com"
private const val SUPPORT_PHONE = "3204784630"

@Composable
fun TermsAndPrivacyScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Términos y Privacidad", onNavigateBack = onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SupportCard(title = "Términos de uso") {
                Text(
                    "UrbanGuard permite administrar información y funciones relacionadas con visitantes, " +
                        "residentes, novedades, rondas y seguridad del conjunto residencial. El usuario debe " +
                        "proteger sus datos de acceso y utilizar la aplicación únicamente para fines autorizados."
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "El usuario es responsable de la veracidad de la información registrada y de solicitar " +
                        "autorización antes de compartir datos personales de terceros, como visitantes o acompañantes."
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "BEJA puede actualizar estas condiciones para reflejar cambios en la aplicación o " +
                        "en su operación. Las reglas vigentes estarán disponibles dentro de UrbanGuard."
                )
            }
            SupportCard(title = "Privacidad de datos") {
                Text(
                    "UrbanGuard recopila datos necesarios para identificar al usuario, gestionar su rol, " +
                        "prestar los servicios de acceso, visitantes, rondas y novedades, y mantener la " +
                        "seguridad de la aplicación."
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Los datos pueden incluir nombre, correo, teléfono, rol dentro del conjunto, registros " +
                        "de visitantes, novedades, rondas y actividad relacionada con el uso de la aplicación."
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "BEJA utiliza esta información para administrar UrbanGuard, prestar soporte, cumplir " +
                        "obligaciones legales y proteger a los usuarios y conjuntos registrados."
                )
            }
            SupportCard(title = "Derechos y contacto") {
                Text(
                    "De acuerdo con la legislación colombiana, puedes solicitar información, actualización, " +
                        "rectificación o eliminación de tus datos personales, cuando sea procedente."
                )
                Spacer(Modifier.height(12.dp))
                Text("Para consultas sobre datos personales, puedes comunicarte con BEJA:")
                Spacer(Modifier.height(12.dp))
                ContactRow(Icons.Default.Mail, "Correo", SUPPORT_EMAIL) {
                    context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = "mailto:$SUPPORT_EMAIL".toUri() })
                }
                ContactRow(Icons.Default.Phone, "Teléfono", SUPPORT_PHONE) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$SUPPORT_PHONE".toUri()))
                }
            }
        }
    }
}

@Composable
fun HelpScreen(role: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Centro de ayuda", onNavigateBack = onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Preguntas frecuentes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UgNavy)
            questionsForRole(role).forEach { faq -> FaqItem(faq) }
            Spacer(Modifier.height(4.dp))
            Text("Contacto de soporte", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UgNavy)
            SupportCard(title = "BEJA") {
                ContactRow(Icons.Default.Mail, "Correo", SUPPORT_EMAIL) {
                    context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = "mailto:$SUPPORT_EMAIL".toUri() })
                }
                ContactRow(Icons.Default.Phone, "Teléfono", SUPPORT_PHONE) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$SUPPORT_PHONE".toUri()))
                }
            }
        }
    }
}

private data class Faq(val question: String, val answer: String)

private fun questionsForRole(role: String): List<Faq> = when (role.lowercase()) {
    "admin" -> adminQuestions()
    "guard" -> guardQuestions()
    else -> residentQuestions()
}

private fun adminQuestions(): List<Faq> = listOf(
        Faq(
            "¿Cómo registro un guardia?",
            "Ingresa al menú Seguridad y selecciona Registrar guardia. " +
                "Completa los datos requeridos para crear su acceso."
        ),
        Faq(
            "¿Cómo reviso novedades?",
            "Desde Seguridad puedes consultar las novedades reportadas por los guardias."
        ),
        Faq(
            "¿Cómo configuro puntos NFC?",
            "En Seguridad selecciona Puntos de control NFC para crear, consultar " +
                "o modificar los puntos del conjunto."
        ),
        Faq(
            "¿Cómo creo una ruta de patrullaje?",
            "Abre Seguridad y selecciona Rutas de patrullaje para configurar " +
                "los puntos que forman cada ruta."
        ),
        Faq(
            "¿Cómo consulto rondas realizadas?",
            "Desde Seguridad puedes revisar el historial de patrullaje disponible."
        ),
        Faq("¿Cómo cambio mi contraseña?", "En Perfil, abre Seguridad y selecciona Contraseña y acceso."),
        Faq("¿Cómo solicito ayuda?", "Puedes escribir a beja@gmail.com o llamar al 3204784630.")
    )

private fun guardQuestions(): List<Faq> = listOf(
        Faq(
            "¿Cómo escaneo una invitación?",
            "Desde el menú de Visitantes selecciona escanear QR y apunta la cámara " +
                "al código presentado por el visitante."
        ),
        Faq(
            "¿Qué hago si el código no es válido?",
            "Verifica que el visitante tenga una invitación activa y que esté " +
                "usando el código correcto."
        ),
        Faq(
            "¿Cómo inicio una ronda?",
            "En Seguridad selecciona la ronda disponible y comienza el recorrido " +
                "desde la opción indicada."
        ),
        Faq(
            "¿Qué hago si no puedo leer un punto NFC?",
            "Verifica que el teléfono tenga NFC activado y que esté cerca del punto. " +
                "Si persiste, reporta la novedad."
        ),
        Faq(
            "¿Cómo reporto una novedad?",
            "Desde Seguridad o los accesos rápidos de inicio puedes registrar " +
                "la novedad y describir lo ocurrido."
        ),
        Faq("¿Cómo cambio mi contraseña?", "En Perfil, abre Seguridad y selecciona Contraseña y acceso."),
        Faq("¿Cómo solicito ayuda?", "Puedes escribir a beja@gmail.com o llamar al 3204784630.")
    )

private fun residentQuestions(): List<Faq> = listOf(
        Faq(
            "¿Cómo creo una invitación de visitante?",
            "En Visitantes selecciona la opción para generar un pase, completa " +
                "la información y comparte el código generado."
        ),
        Faq(
            "¿Dónde veo mis invitaciones?",
            "Desde la sección Visitantes puedes consultar las invitaciones que has creado."
        ),
        Faq(
            "¿Por qué no pudo ingresar mi visitante?",
            "Revisa que la invitación esté vigente, que el código sea el correcto " +
                "y que los datos estén completos."
        ),
        Faq("¿Cómo veo las noticias del conjunto?", "Abre la sección Noticias desde la barra de navegación."),
        Faq(
            "¿Cómo reporto una novedad?",
            "Puedes usar las opciones de novedades o incidencias disponibles en la aplicación."
        ),
        Faq("¿Cómo cambio mi contraseña?", "En Perfil, abre Seguridad y selecciona Contraseña y acceso."),
        Faq(
            "¿Cómo solicito la modificación o eliminación de mis datos?",
            "Escribe a beja@gmail.com indicando la solicitud y los datos que deseas actualizar o eliminar."
        ),
        Faq("¿Cómo solicito ayuda?", "Puedes escribir a beja@gmail.com o llamar al 3204784630.")
    )

@Composable
private fun FaqItem(faq: Faq) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.clickable { expanded = !expanded }.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = faq.question,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    color = UgNavy
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = UgEmerald
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(faq.answer, modifier = Modifier.padding(top = 12.dp), color = UgTextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SupportCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UgNavy)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = UgEmerald, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = UgTextSecondary)
            Text(value, fontWeight = FontWeight.SemiBold, color = UgNavy)
        }
    }
}

