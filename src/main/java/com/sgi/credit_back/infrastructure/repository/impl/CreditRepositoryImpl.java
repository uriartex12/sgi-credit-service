package com.sgi.credit_back.infrastructure.repository.impl;

import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.credit_back.domain.model.Credit;
import com.sgi.credit_back.domain.ports.out.CreditRepository;
import com.sgi.credit_back.infrastructure.mapper.CreditMapper;
import com.sgi.credit_back.infrastructure.repository.CreditRepositoryJPA;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Repository
@Slf4j
public class CreditRepositoryImpl implements CreditRepository {

    @Value("${feign.client.config.transaction-service.url}")
    private String transactionServiceUrl;
    private final WebClient.Builder webClientBuilder;
    private final CreditRepositoryJPA creditRepository;
    private WebClient webClient;

    public CreditRepositoryImpl(WebClient.Builder webClientBuilder, CreditRepositoryJPA creditRepository) {
        this.webClientBuilder = webClientBuilder;
        this.creditRepository = creditRepository;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.baseUrl(transactionServiceUrl).build();
    }

    @Override
    public Mono<CreditResponse> createCredit(Mono<CreditRequest> credit) {
        return credit.flatMap(creditMono -> {
            Credit creditBank = CreditMapper.INSTANCE.map(creditMono);
            creditBank.setCreditNumber(generateAccountNumber());
            creditBank.setCreationDate(LocalDateTime.now());
            creditBank.setUpdatedDate(LocalDateTime.now());
            return creditRepository.save(creditBank).map(CreditMapper.INSTANCE::map);
        });
    }

    @Override
    public Mono<Void> deleteCredit(String id) {
        return creditRepository.findById(id)
                .flatMap(creditRepository::delete)
                .switchIfEmpty(Mono.error(new Exception("Bank Credit not found")));
    }

    @Override
    public Flux<CreditResponse> getAllCredits() {
        return creditRepository.findAll().map(CreditMapper.INSTANCE::map);
    }

    @Override
    public Mono<CreditResponse> getCreditById(String id) {
        return creditRepository.findById(id).map(CreditMapper.INSTANCE::map);
    }

    @Override
    public Mono<CreditResponse> updateCredit(String id, Mono<CreditRequest> customer) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new Exception("Bank Credit not found")))
                .flatMap(accountRequest ->
                        customer.map(updatedAccount -> {
                            Credit updateCredit = CreditMapper.INSTANCE.map(updatedAccount);
                            updateCredit.setUpdatedDate(LocalDateTime.now());
                            updateCredit.setId(accountRequest.getId());
                            return updateCredit;
                        })
                ).flatMap(creditRepository::save).map(CreditMapper.INSTANCE::map);
    }

    @Override
    public Mono<TransactionResponse> withdrawFromCredit(String idCredit, Mono<TransactionRequest> transactionRequest) {
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new Exception("Bank Credit Not Found: " + idCredit))))
                .flatMap(credit -> transactionRequest
                        .filter(transaction -> credit.getAmount().compareTo(BigDecimal.valueOf(transaction.getAmount())) >= 0)
                        .switchIfEmpty(Mono.error(new Exception("Insufficient balance for transaction")))
                        .flatMap(transaction -> {
                            BigDecimal updatedBalance = credit.getAmount().subtract(BigDecimal.valueOf(transaction.getAmount()));
                            transaction.setClientId(credit.getClientId());
                            transaction.setBalance(updatedBalance.doubleValue());
                            credit.setAmount(updatedBalance);
                            return creditRepository.save(credit)
                                    .flatMap(savedAccount -> webClient.post()
                                            .uri("/v1/transaction")
                                            .bodyValue(transaction)
                                            .retrieve()
                                            .bodyToMono(TransactionResponse.class)
                                            .doOnNext(response -> log.info("Transaction saved successfully: {}", response))
                                            .onErrorResume(ex -> {
                                                log.error("Error during transaction process", ex);
                                                return Mono.error(new Exception("Error processing transaction", ex));
                                            }));
                        }));
    }

    @Override
    public Mono<BalanceResponse> getClientBalances(String idCredit) {
        return creditRepository.findById(idCredit)
                .map(CreditMapper.INSTANCE::balance);
    }

    @Override
    public Flux<TransactionResponse> getClientTransactions(String idCredit) {
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.error(new Exception("Bank Credit Not Found: " + idCredit)))
                .flatMapMany(credit -> webClient.get()
                        .uri("/v1/{accountId}/transaction", credit.getId())
                        .retrieve()
                        .bodyToFlux(TransactionResponse.class)
                        .doOnNext(response -> log.info("Transaction: {}", response))
                        .onErrorResume(ex -> {
                            log.error("Error during transaction process", ex);
                            return Flux.error(new Exception("Error processing list transactions", ex));
                        }));
    }

    private String generateAccountNumber() {
        return String.format("%04d00%012d", new Random().nextInt(10000), new Random().nextLong(1000000000000L));
    }
}