package com.sgi.credit.domain.ports.out;

import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface defining operations to manage credits.
 */
public interface DebtRepository {

    Mono<DebtResponse> save(Debt debt);

    Flux<DebtResponse> findAllByClientId(String clientId);

    Mono<DebtResponse> findByCreditId(String creditId);

    Mono<Debt> findByClientIdAndStatus(String clientId, String status);
}
