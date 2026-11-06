package co.edu.unicauca.urbanguard.domain.usecase.packages

import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import javax.inject.Inject

class RegisterPackageDeliveryUseCase @Inject constructor(private val repository: PackageRepository) {
    suspend operator fun invoke(complexId: String, packageId: String, deliveredTo: String): Result<Package> =
        repository.deliver(complexId, packageId, deliveredTo)
}
