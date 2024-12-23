package com.sgi.credit_back.infrastructure.controller;


import com.sgi.bank_account_back.infrastructure.controller.V1Api;
import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.credit_back.domain.ports.in.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CreditController implements V1Api {

    private final CreditService creditService;

    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(Mono<CreditRequest> creditRequest, ServerWebExchange exchange) {
        return creditService.createCredit(creditRequest);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCredit(String creditId, ServerWebExchange exchange) {
        return creditService.deleteCredit(creditId);
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits(ServerWebExchange exchange) {
        return creditService.getAllCredits();
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getClientBalances(String creditId, ServerWebExchange exchange) {
        return creditService.getClientBalances(creditId);
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getClientTransactions(String creditId, ServerWebExchange exchange) {
        return creditService.getClientTransactions(creditId);
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(String creditId, ServerWebExchange exchange) {
        return creditService.getCreditById(creditId);
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> updateCredit(String creditId, Mono<CreditRequest> creditRequest, ServerWebExchange exchange) {
        return creditService.updateCredit(creditId,creditRequest);
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> withdrawFromCredit(String creditId, Mono<TransactionRequest> transactionRequest, ServerWebExchange exchange) {
        return creditService.withdrawFromCredit(creditId,transactionRequest);
    }
}
