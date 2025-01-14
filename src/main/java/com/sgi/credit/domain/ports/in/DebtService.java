package com.sgi.credit.domain.ports.in;

import com.sgi.credit.infrastructure.dto.DebtRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import reactor.core.publisher.Mono;

/**
 * Interface defining the operations for managing debts within the system.
 * Provides methods for creating, hasOverdueDebt.
 */
public interface DebtService {

    Mono<DebtResponse> createDebt(Mono<DebtRequest> debt);

    Mono<DebtResponse> hasOverdueDebt(String clientId);
}
