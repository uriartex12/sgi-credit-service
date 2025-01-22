package com.sgi.credit.infrastructure.repository.impl;

import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.domain.ports.out.DebtRepository;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import com.sgi.credit.infrastructure.mapper.DebtMapper;
import com.sgi.credit.infrastructure.repository.DebtRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of the Debt repository.
 * Provides methods to save, find, delete, and list Debts reactively.
 * Uses DebtRepositoryJpa to interact with the database.
 */
@Repository
@RequiredArgsConstructor
public class DebtRepositoryImpl implements DebtRepository {

    private final DebtRepositoryJpa repositoryJpa;

    @Override
    public Mono<DebtResponse> save(Debt debt) {
        return repositoryJpa.save(debt)
            .map(DebtMapper.INSTANCE::toDebtResponse);
    }

    @Override
    public Flux<DebtResponse> findAllByClientId(String clientId) {
        return repositoryJpa.findAllByClientId(clientId)
                .map(DebtMapper.INSTANCE::toDebtResponse);
    }

    @Override
    public Mono<DebtResponse> findByCreditId(String creditId) {
        return repositoryJpa.findByCreditId(creditId)
                .map(DebtMapper.INSTANCE::toDebtResponse);
    }

    @Override
    public Mono<Debt> findByClientIdAndStatus(String clientId, String status) {
        return repositoryJpa.findByClientIdAndStatus(clientId, status);
    }

}
