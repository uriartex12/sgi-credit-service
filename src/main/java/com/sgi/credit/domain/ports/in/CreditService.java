package com.sgi.credit.domain.ports.in;

import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface defining the operations for managing credits within the system.
 * Provides methods for creating, deleting, updating, and retrieving credit information.
 * Also includes functionality for managing payments, balances, transactions,
 * and credit card charges related to credits.
 */
public interface CreditService {

    Mono<CreditResponse> createCredit(Mono<CreditRequest> credit);

    Mono<Void> deleteCredit(String id);

    Flux<CreditResponse> getAllCredits();

    Mono<CreditResponse> getCreditById(String id);

    Mono<CreditResponse> updateCredit(String id, Mono<CreditRequest> credit);

    Mono<TransactionResponse> makePayment(String idAccount, Mono<PaymentRequest> transactionRequest);

    Mono<BalanceResponse> getClientBalances(String creditId);

    Flux<TransactionResponse> getClientTransactions(String creditId);

    Mono<TransactionResponse> chargeCreditCard(String idCredit, Mono<ChargeRequest> chargeRequestMono);

    Flux<CreditResponse> getCreditCardByClientId(String clientId);
}
