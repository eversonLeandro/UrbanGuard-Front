package co.edu.unicauca.urbanguard.ui.features.residential

import co.edu.unicauca.urbanguard.domain.usecase.residential.RegisterUnitUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.SetOwnerTenantActiveUseCase
import co.edu.unicauca.urbanguard.domain.usecase.residential.UpdateUnitUseCase
import javax.inject.Inject

/** Casos de uso que modifican apartamentos y sus residentes en el censo. */
class CensusUnitUseCases @Inject constructor(
    val registerUnit: RegisterUnitUseCase,
    val updateUnit: UpdateUnitUseCase,
    val setOwnerTenantActive: SetOwnerTenantActiveUseCase
)
