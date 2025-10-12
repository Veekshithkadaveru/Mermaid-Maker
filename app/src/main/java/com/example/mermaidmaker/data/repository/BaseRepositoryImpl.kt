package com.example.mermaidmaker.data.repository

import com.example.mermaidmaker.data.mapper.EntityDomainMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Base repository implementation that provides common CRUD operations.
 * This class reduces duplication across repository implementations by providing
 * generic methods for entity-domain conversions and Flow mappings.
 *
 * @param E Entity type (data layer)
 * @param D Domain type (domain layer)
 * @param mapper The mapper for converting between entity and domain types
 */
abstract class BaseRepositoryImpl<E, D>(
    protected val mapper: EntityDomainMapper<E, D>
) {

    /**
     * Maps a Flow of entity lists to a Flow of domain lists.
     *
     * @param entitiesFlow Flow of entity lists from the data source
     * @return Flow of domain lists
     */
    protected fun mapEntitiesToDomains(entitiesFlow: Flow<List<E>>): Flow<List<D>> {
        return entitiesFlow.map { entities ->
            mapper.entitiesToDomains(entities)
        }
    }

    /**
     * Maps a nullable entity to a nullable domain model.
     *
     * @param entity The entity to map (can be null)
     * @return The corresponding domain model (null if entity is null)
     */
    protected fun mapEntityToDomain(entity: E?): D? {
        return entity?.let { mapper.entityToDomain(it) }
    }

    /**
     * Maps a domain model to its corresponding entity.
     *
     * @param domain The domain model to map
     * @return The corresponding entity
     */
    protected fun mapDomainToEntity(domain: D): E {
        return mapper.domainToEntity(domain)
    }

    /**
     * Maps a list of domain models to a list of entities.
     *
     * @param domains The list of domain models to map
     * @return The list of corresponding entities
     */
    protected fun mapDomainsToEntities(domains: List<D>): List<E> {
        return mapper.domainsToEntities(domains)
    }
}