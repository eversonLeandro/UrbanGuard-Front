package co.edu.unicauca.urbanguard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person

val adminNavItems: List<NavItem> = listOf(
    NavItem(route = Routes.HOME, label = "Inicio", icon = Icons.Default.Home),
    NavItem(route = Routes.CENSUS, label = "Residentes", icon = Icons.Default.Person),
    NavItem(route = Routes.SECURITY, label = "Seguridad", icon = Icons.Default.Lock),
    NavItem(route = Routes.NEWS, label = "Noticias", icon = Icons.Default.Notifications),
    NavItem(route = Routes.PROFILE, label = "Perfil", icon = Icons.Default.Person)
)

// TODO: definir los ítems del menú de Residente cuando se implemente
val residentNavItems: List<NavItem> = emptyList()

// TODO: definir los ítems del menú de Vigilante cuando se implemente
val vigilanteNavItems: List<NavItem> = emptyList()