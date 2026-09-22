package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.navigation.NavItem
import co.edu.unicauca.urbanguard.ui.theme.UgCardGlassBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Barra de navegación inferior flotante tipo cápula de alta gama.
 */
@Composable
fun BottomNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp), ambientColor = UgNavy.copy(alpha = 0.2f), spotColor = UgNavy.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(1.dp, UgCardGlassBorder, RoundedCornerShape(26.dp))
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UgEmerald,
                        selectedTextColor = UgNavy,
                        indicatorColor = UgEmerald.copy(alpha = 0.14f),
                        unselectedIconColor = UgTextSecondary,
                        unselectedTextColor = UgTextSecondary
                    )
                )
            }
        }
    }
}