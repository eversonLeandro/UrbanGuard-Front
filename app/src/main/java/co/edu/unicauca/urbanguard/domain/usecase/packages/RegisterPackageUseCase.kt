package co.edu.unicauca.urbanguard.domain.usecase.packages

import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import javax.inject.Inject

class RegisterPackageUseCase @Inject constructor(private val repository: PackageRepository) {
    suspend operator fun invoke(
        complexId: String,
        residentialUnitId: String?,
        residentialUnitLabel: String?,
        residentId: String?,
        recipientName: String,
        sender: String?,
        description: String,
        observations: String?,
        photoPath: String?
    ): Result<Package> = repository.register(
        complexId, residentialUnitId, residentialUnitLabel, residentId,
        recipientName, sender, description, observations, photoPath
    )
}
