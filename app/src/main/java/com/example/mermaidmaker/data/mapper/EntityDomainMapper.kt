package com.example.mermaidmaker.data.mapper

/**
 * Interface for mapping between entity and domain models.
 * This provides a contract for bidirectional conversion between data layer entities
 * and domain layer models.
 *
 * @param E Entity type (data layer)
 * @param D Domain type (domain layer)
 */
interface EntityDomainMapper<E, D> {
    /**
     * Converts an entity to its corresponding domain model.
     *
     * @param entity The entity to convert
     * @return The corresponding domain model
     */
    fun entityToDomain(entity: E): D

    /**
     * Converts a domain model to its corresponding entity.
     *
     * @param domain The domain model to convert
     * @return The corresponding entity
     */
    fun domainToEntity(domain: D): E

    /**
     * Converts a list of entities to a list of domain models.
     *
     * @param entities The list of entities to convert
     * @return The list of corresponding domain models
     */
    fun entitiesToDomains(entities: List<E>): List<D> {
        return entities.map { entityToDomain(it) }
    }

    /**
     * Converts a list of domain models to a list of entities.
     *
     * @param domains The list of domain models to convert
     * @return The list of corresponding entities
     */
    fun domainsToEntities(domains: List<D>): List<E> {
        return domains.map { domainToEntity(it) }
    }
}