package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageMetrics
import co.edu.unicauca.urbanguard.domain.model.PackageRegistration

interface PackageRepository {
    suspend fun metrics(complexId: String, from: String? = null, to: String? = null): Result<PackageMetrics>
    suspend fun list(complexId: String, status: String? = null): Result<List<Package>>
    suspend fun detail(complexId: String, packageId: String): Result<Package>
    suspend fun register(complexId: String, registration: PackageRegistration): Result<Package>
    suspend fun deliver(complexId: String, packageId: String, deliveredTo: String): Result<Package>
}
