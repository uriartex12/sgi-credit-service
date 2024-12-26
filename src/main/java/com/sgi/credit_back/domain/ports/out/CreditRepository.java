package com.sgi.credit_back.domain.ports.out;

import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.credit_back.domain.model.Credit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepository {
    Mono<CreditResponse> save(Credit credit);
    Mono<Credit> findById(String id);
    Flux<CreditResponse> findAll();
    Mono<Void> delete(Credit credit);
}
