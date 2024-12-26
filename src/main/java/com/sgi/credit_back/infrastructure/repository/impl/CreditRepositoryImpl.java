package com.sgi.credit_back.infrastructure.repository.impl;

import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.credit_back.domain.model.Credit;
import com.sgi.credit_back.domain.ports.out.CreditRepository;
import com.sgi.credit_back.infrastructure.mapper.CreditMapper;
import com.sgi.credit_back.infrastructure.repository.CreditRepositoryJPA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditRepositoryImpl implements CreditRepository {

    private final CreditRepositoryJPA repositoryJPA;

    @Override
    public Mono<CreditResponse> save(Credit credit) {
        return repositoryJPA.save(credit)
                .map(CreditMapper.INSTANCE::map);
    }
    @Override
    public Mono<Credit> findById(String id) {
        return repositoryJPA.findById(id);
    }

    @Override
    public Flux<CreditResponse> findAll() {
        return repositoryJPA.findAll()
                .map(CreditMapper.INSTANCE::map);
    }

    @Override
    public Mono<Void> delete(Credit credit) {
        return repositoryJPA.delete(credit);
    }
}