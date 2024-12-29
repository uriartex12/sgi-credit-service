package com.sgi.credit.infrastructure.controller;

import com.sgi.credit.domain.ports.in.CreditService;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller to handle operations related to credits.
 */
@RestController
@RequiredArgsConstructor
public class CreditController implements V1Api {

    private final CreditService creditService;

    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(
            Mono<CreditRequest> creditRequest, ServerWebExchange exchange) {
        return creditService.createCredit(creditRequest)
                .map(creditResponse -> ResponseEntity.status(HttpStatus.CREATED).body(creditResponse));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCredit(
            String creditId, ServerWebExchange exchange) {
        return creditService.deleteCredit(creditId)
                .map(credit -> ResponseEntity.ok().body(credit));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits(
            ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> ResponseEntity.ok().body(creditService.getAllCredits()));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getClientBalances(
            String creditId, ServerWebExchange exchange) {
        return creditService.getClientBalances(creditId)
                .map(balance -> ResponseEntity.ok().body(balance));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getClientTransactions(
            String creditId, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> ResponseEntity.ok()
                .body(creditService.getClientTransactions(creditId)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(
            String creditId, ServerWebExchange exchange) {
        return creditService.getCreditById(creditId)
                .map(creditResponse -> ResponseEntity.ok().body(creditResponse));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getCreditCardByClientId(String clientId, ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> ResponseEntity.ok()
                .body(creditService.getCreditCardByClientId(clientId)));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> updateCredit(
            String creditId, Mono<CreditRequest> creditRequest, ServerWebExchange exchange) {
        return creditService.updateCredit(creditId, creditRequest)
                .map(creditResponse -> ResponseEntity.ok().body(creditResponse));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> makePayment(
            String creditId, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return creditService.makePayment(creditId, paymentRequest)
                .map(creditResponse -> ResponseEntity.ok().body(creditResponse));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> chargeCreditCard(
            String creditId, Mono<ChargeRequest> chargeRequest, ServerWebExchange exchange) {
        return creditService.chargeCreditCard(creditId, chargeRequest)
                .map(creditResponse -> ResponseEntity.ok().body(creditResponse));
    }
}
