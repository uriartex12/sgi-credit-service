package com.sgi.credit.infrastructure.repository;

import com.sgi.credit.domain.model.Credit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive Repository for the Credit entity.
 * Extends ReactiveMongoRepository to perform CRUD operations in MongoDB.
 */
public interface CreditRepositoryJpa extends ReactiveMongoRepository<Credit, String> {

    Flux<Credit> findAllByClientId(String clientId);
}
