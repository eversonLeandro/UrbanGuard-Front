package co.edu.unicauca.urbanguard.ui.features.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.ui.theme.*
import org.json.JSONArray

@Composable
fun RegisterConjuntoScreen(
    onNavigateToHome: () -> Unit = {},
    viewModel: RegisterConjuntoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var complejoNombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("Popayan") }
    var totalUnidades by remember { mutableStateOf("") }

    val totalUnidadesNum = totalUnidades.toIntOrNull()
    val isFormValid = complejoNombre.isNotBlank() &&
            direccion.isNotBlank() &&
            ciudad.isNotBlank() &&
            (totalUnidadesNum != null && totalUnidadesNum > 0)

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            onNavigateToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgSurfaceNeutral)
    ) {
        Header()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {            
            Spacer(Modifier.height(20.dp))
            GeneralFormSection(
                complejoNombre = complejoNombre,
                onComplejoNombreChange = { complejoNombre = it },
                direccion = direccion,
                onDireccionChange = { direccion = it },
                ciudad = ciudad,
                onCiudadChange = { ciudad = it },
                totalUnidades = totalUnidades,
                onTotalUnidadesChange = { totalUnidades = it }
            )
            Spacer(Modifier.height(24.dp)) 
        }                     
        Footer(
            enabled = isFormValid,
            errorMessage = uiState.errorMessage,
            onCreateClick = {
                viewModel.onCreateClick(
                    nombre = complejoNombre,
                    direccion = direccion,
                    ciudad = ciudad,
                    totalUnidades = totalUnidadesNum ?: 0
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun Footer(
    enabled: Boolean = true,
    errorMessage: String? = null,
    onCreateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFDC2626),
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = onCreateClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = UgViolet),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Crear y continuar",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun GeneralFormSection(
    complejoNombre: String,
    onComplejoNombreChange: (String) -> Unit,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    ciudad: String,
    onCiudadChange: (String) -> Unit,
    totalUnidades: String,
    onTotalUnidadesChange: (String) -> Unit
) {
    val context = LocalContext.current
    val ciudades = remember { loadColombiaCities(context) }

    Column(modifier = Modifier.fillMaxWidth()) {
       Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = UgViolet,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Información general",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgTextPrimary
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "Diligencia los datos del condominio para registrarlo en la plataforma.",
                    fontSize = 12.sp,
                    color = UgTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        InputField(
            label = "Nombre del condomino",
            icon = Icons.Default.Business,
            value = complejoNombre,
            onValueChange = onComplejoNombreChange,
            placeholder = "Ej: Residencial Arboledas"
        )

        Spacer(Modifier.height(16.dp))

        InputField(
            label = "Dirección",
            icon = Icons.Default.Place,
            value = direccion,
            onValueChange = onDireccionChange,
            placeholder = "Calle y número"
        )

        Spacer(Modifier.height(16.dp))

        CityDropdown(
            selected = ciudad,
            onSelected = onCiudadChange,
            options = ciudades
        )

        Spacer(Modifier.height(16.dp))

        InputField(
            label = "Total de unidades",
            icon = Icons.Default.GridView,
            value = totalUnidades,
            onValueChange = onTotalUnidadesChange,
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}


@Composable
private fun FieldLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UgViolet,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextPrimary
        )
    }
}

@Composable
private fun InputField(
    label: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(icon = icon, text = label)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = UgInputBg,
                unfocusedContainerColor = UgInputBg,
                focusedBorderColor = UgViolet.copy(alpha = 0.4f),
                unfocusedBorderColor = Color.Transparent,
                cursorColor = UgViolet
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selected) { mutableStateOf(selected) }

    val filteredOptions = remember(query, options) {
        if (query.isBlank()) options else
            options.filter { it.contains(query, ignoreCase = true) || query.contains(it, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(icon = Icons.Default.Map, text = "Ciudad")
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSelected(it)
                    expanded = true
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                placeholder = {
                    Text(
                        text = "Ciudad",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = UgInputBg,
                    unfocusedContainerColor = UgInputBg,
                    focusedBorderColor = UgViolet.copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = UgViolet
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (filteredOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Sin resultados",
                                fontSize = 14.sp,
                                color = UgTextSecondary
                            )
                        },
                        onClick = { expanded = false }
                    )
                } else {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    color = UgTextPrimary
                                )
                            },
                            onClick = {
                                onSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun loadColombiaCities(context: Context): List<String> {
    val jsonString = context.resources.openRawResource(R.raw.colombia_cities)
        .bufferedReader()
        .use { it.readText() }
    return try {
        JSONArray(jsonString).let { array ->
            buildList {
                repeat(array.length()) {
                    add(array.getString(it))
                }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = UgDivider)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(UgViolet, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.GppGood,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Registrar Condominio",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary
        )
    }
}