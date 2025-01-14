package com.sgi.credit.infrastructure.repository;

import com.sgi.credit.domain.model.Debt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Repository for the Debt entity.
 * Extends ReactiveMongoRepository to perform CRUD operations in MongoDB.
 */
public interface DebtRepositoryJpa extends ReactiveMongoRepository<Debt, String> {

    Mono<Debt> findByCreditId(String creditId);

    Flux<Debt> findAllByClientId(String clientId);

    Mono<Debt> findByCreditIdAndStatus(String creditId, String status);
}
