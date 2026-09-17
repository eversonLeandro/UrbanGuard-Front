package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.model.LinkRequest

interface LinkByCodeRepository {
    suspend fun searchConjunto(code: String): Result<ConjuntoSearch>
    suspend fun requestLink(conjuntoId: String, unidad: String): Result<LinkRequest>
}