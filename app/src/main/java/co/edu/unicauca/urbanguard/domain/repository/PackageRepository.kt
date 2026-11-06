package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.Package

interface PackageRepository {
    suspend fun list(complexId: String, status: String? = null): Result<List<Package>>
    suspend fun detail(complexId: String, packageId: String): Result<Package>
    suspend fun register(
        complexId: String,
        residentialUnitId: String?,
        residentialUnitLabel: String?,
        residentId: String?,
        recipientName: String,
        sender: String?,
        description: String,
        observations: String?,
        photoPath: String?
    ): Result<Package>
    suspend fun deliver(complexId: String, packageId: String, deliveredTo: String): Result<Package>
}
