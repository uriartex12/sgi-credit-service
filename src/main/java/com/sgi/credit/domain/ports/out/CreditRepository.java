package com.sgi.credit.domain.ports.out;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface defining operations to manage credits.
 */
public interface CreditRepository {

    Mono<CreditResponse> save(Credit credit);

    Mono<Credit> findById(String id);

    Flux<CreditResponse> findAll();

    Mono<Void> delete(Credit credit);

    Flux<CreditResponse> getCreditCardByClientId(String  clientId);
}
