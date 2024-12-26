package com.sgi.credit_back.domain.ports.in;

import com.sgi.bank_account_back.infrastructure.dto.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CreditService {
    Mono<CreditResponse> createCredit(Mono<CreditRequest> credit);
    Mono<Void> deleteCredit(String id);
    Flux<CreditResponse> getAllCredits();
    Mono<CreditResponse> getCreditById(String id);
    Mono<CreditResponse> updateCredit(String id, Mono<CreditRequest> credit);
    Mono<TransactionResponse> makePayment(String idAccount, Mono<PaymentRequest> transactionRequest);
    Mono<BalanceResponse> getClientBalances(String idAccount);
    Flux<TransactionResponse> getClientTransactions(String idAccount);
    Mono<TransactionResponse> chargeCreditCard(String idAccount, Mono<ChargeRequest> chargeRequestMono);
}
