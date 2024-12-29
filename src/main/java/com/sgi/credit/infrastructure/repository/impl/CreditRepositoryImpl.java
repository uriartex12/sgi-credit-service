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
 * Implementación del repositorio de créditos.
 * Proporciona métodos para guardar, buscar, eliminar y listar créditos de forma reactiva.
 * Utiliza CreditRepositoryJpa para interactuar con la base de datos.
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
