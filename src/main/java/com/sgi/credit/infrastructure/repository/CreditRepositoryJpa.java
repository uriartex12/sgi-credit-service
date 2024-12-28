package com.sgi.credit.infrastructure.repository;

import com.sgi.credit.domain.model.Credit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Repositorio Reactivo para la entidad Credit.
 * Extiende de ReactiveMongoRepository para realizar operaciones CRUD en MongoDB.
 */
public interface CreditRepositoryJpa extends ReactiveMongoRepository<Credit, String> {
}
