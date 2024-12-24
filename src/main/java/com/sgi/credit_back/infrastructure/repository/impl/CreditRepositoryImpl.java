package com.sgi.credit_back.infrastructure.repository.impl;

import com.sgi.bank_account_back.infrastructure.dto.*;
import com.sgi.bank_account_back.infrastructure.dto.TransactionRequest.*;
import com.sgi.credit_back.domain.model.Credit;
import com.sgi.credit_back.domain.ports.out.CreditRepository;
import com.sgi.credit_back.infrastructure.feign.FeignExternalServiceImpl;
import com.sgi.credit_back.infrastructure.mapper.CreditMapper;
import com.sgi.credit_back.infrastructure.repository.CreditRepositoryJPA;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CreditRepositoryImpl implements CreditRepository {

    private final FeignExternalServiceImpl webClient;
    private final CreditRepositoryJPA creditRepository;

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
    public Flux<TransactionResponse> getClientTransactions(String idCredit) {
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.error(new Exception("Bank Credit Not Found: " + idCredit)))
                .flatMapMany(credit -> webClient.get("/v1/{productId}/transaction",
                        idCredit,
                        TransactionResponse.class));
    }

    @Override
    @Transactional
    public Mono<TransactionResponse> makePayment(String idCredit, Mono<PaymentRequest> paymentRequestMono) {
        TransactionRequest transaction = new TransactionRequest();
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new Exception("Credit product not found: " + idCredit))))
                .flatMap(credit -> paymentRequestMono
                        .filter(payment -> payment.getAmount().compareTo(credit.getAmount()) <= 0)
                        .switchIfEmpty(Mono.error(new Exception("Payment amount must be greater than zero")))
                        .flatMap(payment -> {
                            BigDecimal updatedBalance = credit.getAmount().subtract(payment.getAmount());
                            transaction.setProductId(idCredit);
                            transaction.setClientId(credit.getClientId());
                            transaction.setType(TypeEnum.PAYMENT);
                            transaction.setBalance(updatedBalance.doubleValue());
                            transaction.setAmount(payment.getAmount().doubleValue());
                            credit.setAmount(updatedBalance);
                            return creditRepository.save(credit)
                                    .flatMap(savedAccount -> webClient.post("/v1/transaction",
                                            transaction,
                                            TransactionResponse.class));
                        }));
    }

    @Override
    @Transactional
    public Mono<TransactionResponse> chargeCreditCard(String idCredit, Mono<ChargeRequest> chargeRequestMono) {
        TransactionRequest transaction = new TransactionRequest();
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new Exception("Credit product not found: " + idCredit))))
                .flatMap(credit -> chargeRequestMono
                        .filter(charge -> !isCreditLimitExceeded(credit, charge.getAmount()))
                        .switchIfEmpty(Mono.error(new Exception("Insufficient credit available")))
                        .flatMap(charge -> {
                            BigDecimal updatedBalance = credit.getAmount().add(charge.getAmount());
                            transaction.setProductId(idCredit);
                            transaction.setClientId(credit.getClientId());
                            transaction.setType(TypeEnum.CHARGE);
                            transaction.setBalance(updatedBalance.doubleValue());
                            transaction.setAmount(charge.getAmount().doubleValue());
                            credit.setAmount(updatedBalance);
                            return creditRepository.save(credit)
                                    .flatMap(savedAccount -> webClient.post("/v1/transaction",
                                            transaction,
                                            TransactionResponse.class));
                        }));
    }


    @Override
    public Mono<BalanceResponse> getClientBalances(String idCredit) {
        return creditRepository.findById(idCredit)
                .map(CreditMapper.INSTANCE::balance);
    }

    private boolean isCreditLimitExceeded(Credit credit, BigDecimal requestedAmount) {
        return requestedAmount.compareTo(credit.getCreditLimit().subtract(credit.getAmount())) > 0;
    }



    private String generateAccountNumber() {
        return String.format("%04d00%012d", new Random().nextInt(10000), new Random().nextLong(1000000000000L));
    }
}