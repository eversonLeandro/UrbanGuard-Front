package co.edu.unicauca.urbanguard.domain.usecase.packages

import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageRegistration
import co.edu.unicauca.urbanguard.domain.repository.PackageRepository
import javax.inject.Inject

class RegisterPackageUseCase @Inject constructor(private val repository: PackageRepository) {
    suspend operator fun invoke(complexId: String, registration: PackageRegistration): Result<Package> =
        repository.register(complexId, registration)
}
