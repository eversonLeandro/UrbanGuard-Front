package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.VisitorConstants
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate

private val FIELD_CORNER_RADIUS = 10.dp
private val FIELD_HORIZONTAL_PADDING = 16.dp
private val FIELD_VERTICAL_PADDING = 18.dp
private val FIELD_ICON_SIZE = 18.dp
private val FIELD_ICON_GAP = 12.dp
private val FIELD_TEXT_SIZE = 16.sp
private val DATE_FIELD_LABEL_GAP = 6.dp

/**
 * Formulario de creación de un pase QR. Cuando el backend responde bien, el
 * ViewModel emite [GenerateQrEvent.PassGenerated] y la pantalla navega a la
 * vista del pase; si algo falla, el error se muestra aquí mismo.
 */
@Composable
fun GenerateQrScreen(
    onGenerated: () -> Unit,
    viewModel: GenerateQrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is GenerateQrEvent.PassGenerated) onGenerated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Generar Pase QR",
        )

        PassFormContent(uiState = uiState, viewModel = viewModel)
    }
}

@Composable
private fun PassFormContent(
    uiState: GenerateQrUiState,
    viewModel: GenerateQrViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        SectionHeader()

        Spacer(Modifier.height(20.dp))

        VisitorNameField(
            value = uiState.visitorName,
            isLoading = uiState.isLoading,
            onValueChange = viewModel::onVisitorNameChange
        )

        Spacer(Modifier.height(16.dp))

        GuestCountSelector(
            guestCount = uiState.guestCount,
            isLoading = uiState.isLoading,
            onGuestCountChange = viewModel::onGuestCountChange
        )

        Spacer(Modifier.height(16.dp))

        DateSelectorField(
            date = uiState.date,
            isLoading = uiState.isLoading,
            onDateChange = viewModel::onDateChange
        )

        Spacer(Modifier.height(20.dp))

        VisitorActionButton(
            text = "Generar QR",
            isLoading = uiState.isLoading,
            onClick = viewModel::onGenerateClick
        )

        InlineErrorMessage(errorMessage = uiState.errorMessage)
    }
}

@Composable
private fun SectionHeader() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pase de visitante",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Genera un código de un solo uso válido por 24 horas.",
                    fontSize = 12.sp,
                    color = UgTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun VisitorNameField(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit
) {
    FieldLabel(text = "Nombre del visitante")
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text("Ej: María Gómez", fontSize = 13.sp, color = UgDisabledText)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        enabled = !isLoading,
        colors = fieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GuestCountSelector(
    guestCount: Int,
    isLoading: Boolean,
    onGuestCountChange: (Int) -> Unit
) {
    FieldLabel(text = "Cantidad de invitados")
    Spacer(Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgInputBg, RoundedCornerShape(10.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CountStepButton(
            icon = Icons.Default.Remove,
            contentDescription = "Quitar un invitado",
            enabled = !isLoading && guestCount > VisitorConstants.MIN_GUESTS,
            onClick = { onGuestCountChange(guestCount - 1) }
        )
        Text(
            text = guestCount.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
        CountStepButton(
            icon = Icons.Default.Add,
            contentDescription = "Agregar un invitado",
            enabled = !isLoading && guestCount < VisitorConstants.MAX_GUESTS,
            onClick = { onGuestCountChange(guestCount + 1) }
        )
    }
}

@Composable
private fun CountStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = if (enabled) Color.White else UgInputBg,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) UgEmerald else UgDisabledText,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Campo de fecha de vigencia.
 *
 * No se usa `OutlinedTextField` porque el campo consume el gesto de pulsación y
 * el `clickable` exterior nunca se dispara, lo que dejaba la fecha sin poder
 * editar. En su lugar se dibuja una superficie con el mismo estilo visual
 * (borde, fondo y tipografía del tema) que sí recibe el toque y abre el
 * calendario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorField(
    date: LocalDate,
    isLoading: Boolean,
    onDateChange: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    FieldLabel(text = "Fecha de vigencia")
    Spacer(Modifier.height(DATE_FIELD_LABEL_GAP))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(FIELD_CORNER_RADIUS))
            .border(1.dp, UgBorder, RoundedCornerShape(FIELD_CORNER_RADIUS))
            .clickable(enabled = !isLoading) { showDatePicker = true }
            .padding(horizontal = FIELD_HORIZONTAL_PADDING, vertical = FIELD_VERTICAL_PADDING),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(FIELD_ICON_SIZE)
        )
        Spacer(Modifier.width(FIELD_ICON_GAP))
        Text(
            text = date.format(VisitorConstants.DATE_LABEL_FORMAT),
            fontSize = FIELD_TEXT_SIZE,
            color = if (isLoading) UgDisabledText else UgTextPrimary
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toUtcMillis(),
            selectableDates = remember { TodayForwardSelectableDates() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateChange(fromUtcMillis(it))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = UgNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = UgTextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Spacer(Modifier.height(DATE_FIELD_LABEL_GAP))
    Text(
        text = "Solo puedes elegir el día de hoy o un día posterior.",
        fontSize = 11.sp,
        color = UgTextSecondary
    )
}

@Composable
private fun InlineErrorMessage(errorMessage: String?) {
    Spacer(Modifier.height(12.dp))
    if (errorMessage != null) {
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = UgErrorText,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = UgTextPrimary
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = UgNavy,
    unfocusedBorderColor = UgBorder,
    errorBorderColor = UgErrorBright,
    cursorColor = UgNavy
)


