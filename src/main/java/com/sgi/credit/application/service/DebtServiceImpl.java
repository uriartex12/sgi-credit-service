package com.sgi.credit.application.service;

import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.domain.ports.in.DebtService;
import com.sgi.credit.domain.ports.out.DebtRepository;
import com.sgi.credit.infrastructure.dto.DebtRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import com.sgi.credit.infrastructure.mapper.DebtMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service implementation for managing Debts.
 */
@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {

    private final DebtRepository debtRepository;

    @Override
    public Mono<DebtResponse> createDebt(Mono<DebtRequest> debtRequestMono) {
        return debtRequestMono.flatMap(debtRequest -> {
            Debt debt = DebtMapper.INSTANCE.toDebt(Mono.just(debtRequest));
            return debtRepository.save(debt);
        });
    }

    @Override
    public Mono<DebtResponse> hasOverdueDebt(String clientId) {
        return debtRepository.findByCreditIdAndStatus(clientId, DebtRequest.StatusEnum.EXPIRED.name())
                .map(DebtMapper.INSTANCE::toDebtResponse);
    }
}
