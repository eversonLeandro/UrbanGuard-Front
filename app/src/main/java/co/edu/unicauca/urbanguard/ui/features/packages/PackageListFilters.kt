package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageStatus
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class PackageFilterActions(
    val onSearchQueryChange: (String) -> Unit,
    val onClearDate: () -> Unit,
    val onShowDatePicker: () -> Unit,
    val onStatusMenuExpandedChange: (Boolean) -> Unit,
    val onStatusChange: (PackageStatus?) -> Unit
)

data class PackageFilterValues(
    val searchQuery: String,
    val selectedDate: LocalDate?,
    val selectedStatus: PackageStatus?,
    val statusMenuExpanded: Boolean
)

@Composable
fun PackageFilterBar(
    values: PackageFilterValues,
    actions: PackageFilterActions
) {
    OutlinedTextField(
        value = values.searchQuery,
        onValueChange = actions.onSearchQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar residente") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, null, tint = UgTextSecondary) },
        trailingIcon = packageSearchTrailingIcon(values.searchQuery, actions.onSearchQueryChange),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = UgSurface,
            unfocusedContainerColor = UgSurface
        )
    )
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DateFilterField(
            date = values.selectedDate,
            onShowDatePicker = actions.onShowDatePicker,
            modifier = Modifier.weight(1f).height(56.dp),
            onClearDate = actions.onClearDate,
            placeholder = "Fecha"
        )
        PackageStatusFilter(values, actions)
    }
}

@Composable
private fun packageSearchTrailingIcon(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
): (@Composable (() -> Unit))? = if (searchQuery.isNotEmpty()) {
    {
        Icon(
            Icons.Default.Close,
            null,
            tint = UgTextSecondary,
            modifier = Modifier.size(20.dp).clickable { onSearchQueryChange("") }
        )
    }
} else null

@Composable
private fun RowScope.PackageStatusFilter(
    values: PackageFilterValues,
    actions: PackageFilterActions
) {
    Box(modifier = Modifier.weight(1f)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .background(UgSurface, RoundedCornerShape(12.dp))
                .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
                .clickable { actions.onStatusMenuExpandedChange(true) }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            PackageStatusFilterContent(values.selectedStatus)
        }
        PackageStatusMenu(values.statusMenuExpanded, actions)
    }
}

@Composable
private fun PackageStatusMenu(
    expanded: Boolean,
    actions: PackageFilterActions
) {
    DropdownMenu(expanded = expanded, onDismissRequest = {
        actions.onStatusMenuExpandedChange(false)
    }) {
        DropdownMenuItem(
            text = { Text("Todos") },
            onClick = { actions.onStatusChange(null); actions.onStatusMenuExpandedChange(false) }
        )
        PackageStatus.entries.forEach { status ->
            DropdownMenuItem(
                text = {
                    Text(status.name.lowercase().replaceFirstChar { it.uppercase() })
                },
                onClick = { actions.onStatusChange(status); actions.onStatusMenuExpandedChange(false) }
            )
        }
    }
}

@Composable
private fun PackageStatusFilterContent(selectedStatus: PackageStatus?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Estado", color = UgTextSecondary, fontSize = 11.sp)
            Text(
                text = selectedStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                    ?: "Todos",
                color = UgNavy,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        Icon(Icons.Default.KeyboardArrowDown, null, tint = UgTextSecondary,
            modifier = Modifier.size(18.dp))
    }
}

fun filterPackages(
    packages: List<Package>,
    query: String,
    selectedDate: LocalDate?,
    selectedStatus: PackageStatus?
): List<Package> = packages.filter { parcel ->
    val matchesResident = matchesPackageResident(parcel, query)
    val matchesDate = matchesPackageDate(parcel.receivedAt, selectedDate)
    val matchesStatus = selectedStatus == null || parcel.status == selectedStatus
    matchesResident && matchesDate && matchesStatus
}

private fun matchesPackageResident(parcel: Package, query: String): Boolean =
    query.isBlank() ||
        parcel.recipientName.contains(query.trim(), ignoreCase = true) ||
        parcel.residentialUnitLabel?.contains(query.trim(), ignoreCase = true) == true

private fun matchesPackageDate(receivedAt: String, selectedDate: LocalDate?): Boolean =
    selectedDate == null || runCatching {
        Instant.parse(receivedAt).atZone(ZoneOffset.UTC).toLocalDate() == selectedDate
    }.getOrDefault(receivedAt.startsWith(selectedDate.toString()))
