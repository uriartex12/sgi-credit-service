package com.sgi.credit_back.application.service;
import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.credit_back.domain.ports.in.CreditService;
import com.sgi.credit_back.domain.ports.out.CreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;

    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(Mono<CreditRequest> credit) {
        return creditRepository.createCredit(credit)
                .map(createdCredit ->ResponseEntity.ok().body(createdCredit))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCredit(String id) {
        return creditRepository.deleteCredit(id)
                .map(deleteCredit-> ResponseEntity.ok().body(deleteCredit))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<CreditResponse>>> getAllCredits() {
        return Mono.fromSupplier(() -> ResponseEntity.ok().body(creditRepository.getAllCredits()))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> getCreditById(String id) {
        return  creditRepository.getCreditById(id)
                .map(creditResponse ->ResponseEntity.ok().body(creditResponse))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> updateCredit(String id, Mono<CreditRequest> credit) {
        return  creditRepository.updateCredit(id,credit)
                .map(creditResponse ->ResponseEntity.ok().body(creditResponse))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> makePayment(String idAccount, Mono<PaymentRequest> paymentRequestMono) {
        return creditRepository.makePayment(idAccount, paymentRequestMono)
                .map(transactionResponse ->ResponseEntity.ok().body(transactionResponse))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getClientBalances(String idAccount) {
        return creditRepository.getClientBalances(idAccount)
                .map(balanceResponse ->ResponseEntity.ok().body(balanceResponse))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionResponse>>> getClientTransactions(String idAccount) {
        return creditRepository.getClientTransactions(idAccount)
                .collectList()
                .map(transactions -> ResponseEntity.ok().body(Flux.fromIterable(transactions)))
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Flux.empty()));
                });
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> chargeCreditCard(String idAccount, Mono<ChargeRequest> chargeRequestMono) {
        return creditRepository.chargeCreditCard(idAccount, chargeRequestMono)
                .map(transactionResponse ->ResponseEntity.ok().body(transactionResponse))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
