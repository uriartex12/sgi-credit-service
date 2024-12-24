package com.sgi.credit_back.domain.ports.in;

import com.sgi.bank_account_back.infrastructure.dto.*;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CreditService {
    Mono<ResponseEntity<CreditResponse>> createCredit(Mono<CreditRequest> credit);
    Mono<ResponseEntity<Void>> deleteCredit(String id);
    Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits();
    Mono<ResponseEntity<CreditResponse>> getCreditById(String id);
    Mono<ResponseEntity<CreditResponse>> updateCredit(String id, Mono<CreditRequest> credit);
    Mono<ResponseEntity<TransactionResponse>> makePayment(String idAccount, Mono<PaymentRequest> transactionRequest);
    Mono<ResponseEntity<BalanceResponse>> getClientBalances(String idAccount);
    Mono<ResponseEntity<Flux<TransactionResponse>>> getClientTransactions(String idAccount);
    Mono<ResponseEntity<TransactionResponse>> chargeCreditCard(String idAccount, Mono<ChargeRequest> chargeRequestMono);
}
