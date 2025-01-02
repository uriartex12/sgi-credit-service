package com.sgi.credit.infrastructure.repository.impl;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.domain.ports.out.CreditRepository;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.mapper.CreditMapper;
import com.sgi.credit.infrastructure.repository.CreditRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of the credit repository.
 * Provides methods to save, find, delete, and list credits reactively.
 * Uses CreditRepositoryJpa to interact with the database.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditRepositoryImpl implements CreditRepository {

    private final CreditRepositoryJpa creditRepository;

    @Override
    public Mono<CreditResponse> save(Credit credit) {
        return creditRepository.save(credit)
                .map(CreditMapper.INSTANCE::toCreditResponse);
    }

    @Override
    public Mono<Credit> findById(String id) {
        return creditRepository.findById(id);
    }

    @Override
    public Flux<CreditResponse> findAll() {
        return creditRepository.findAll()
                .map(CreditMapper.INSTANCE::toCreditResponse);
    }

    @Override
    public Mono<Void> delete(Credit credit) {
        return creditRepository.delete(credit);
    }

    @Override
    public Flux<CreditResponse> getCreditCardByClientId(String clientId) {
        return creditRepository.findAllByClientId(clientId)
                .map(CreditMapper.INSTANCE::toCreditResponse);
    }
}
