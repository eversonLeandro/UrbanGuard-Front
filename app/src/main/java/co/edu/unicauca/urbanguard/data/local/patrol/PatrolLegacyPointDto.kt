package co.edu.unicauca.urbanguard.data.local.patrol

/** Forma antigua del punto (incluía `order`); solo se usa para migrar. */
data class PatrolLegacyPointDto(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val nfcTagId: String = "",
    val order: Int = 0,
    val isActive: Boolean = true
)
