package com.sgi.credit.application.service;

import java.math.BigDecimal;
import com.sgi.credit.domain.model.Credit;
import java.time.Instant;
import java.util.function.Predicate;

import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.dto.TransactionRequest;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.sgi.credit.domain.ports.in.CreditService;
import com.sgi.credit.domain.ports.out.CreditRepository;
import com.sgi.credit.domain.ports.out.FeignExternalService;
import com.sgi.credit.domain.shared.CustomError;
import com.sgi.credit.infrastructure.exception.CustomException;
import com.sgi.credit.infrastructure.mapper.CreditMapper;

import static com.sgi.credit.domain.shared.Constants.generateAccountNumber;

/**
 * Service implementation for managing credits.
 */
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final FeignExternalService webClient;

    @Override
    public Mono<CreditResponse> createCredit(Mono<CreditRequest> credit) {
        return credit.flatMap(creditMono -> {
            Credit creditBank = CreditMapper.INSTANCE.toCredit(creditMono, generateAccountNumber());
            return creditRepository.save(creditBank);
        });
    }

    @Override
    public Mono<Void> deleteCredit(String id) {
        return creditRepository.findById(id)
                .flatMap(creditRepository::delete)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND)));
    }

    @Override
    public Flux<CreditResponse> getAllCredits() {
        return creditRepository.findAll();
    }

    @Override
    public Mono<CreditResponse> getCreditById(String id) {
        return creditRepository.findById(id).map(CreditMapper.INSTANCE::toCreditResponse);
    }

    @Override
    public Mono<CreditResponse> updateCredit(String id, Mono<CreditRequest> customer) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND)))
                .flatMap(accountRequest ->
                        customer.map(updatedAccount -> {
                            accountRequest.setCreditLimit(updatedAccount.getCreditLimit());
                            accountRequest.setType(updatedAccount.getType().getValue());
                            accountRequest.setInterestRate(updatedAccount.getInterestRate());
                            accountRequest.setUpdatedDate(Instant.now());
                            return accountRequest;
                        })
                ).flatMap(creditRepository::save);
    }

    @Override
    public Flux<TransactionResponse> getClientTransactions(String idCredit) {
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND)))
                .flatMapMany(credit -> webClient.get(
                        "/v1/{productId}/transaction",
                        idCredit,
                        TransactionResponse.class));
    }

    @Override
    @Transactional
    public Mono<TransactionResponse> makePayment(String idCredit, Mono<PaymentRequest> paymentRequestMono) {
        TransactionRequest transaction = new TransactionRequest();
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND))))
                .flatMap(credit -> paymentRequestMono
                        .filter(payment -> payment.getAmount().compareTo(credit.getConsumptionAmount()) <= 0)
                        .switchIfEmpty(Mono.error(new CustomException(CustomError.E_INVALID_INPUT)))
                        .flatMap(payment -> {
                            BigDecimal updatedConsumptionAmount = credit.getConsumptionAmount().subtract(payment.getAmount());
                            transaction.setProductId(idCredit);
                            transaction.setClientId(credit.getClientId());
                            transaction.setType(TransactionRequest.TypeEnum.PAYMENT);
                            transaction.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount).doubleValue());
                            transaction.setAmount(payment.getAmount().doubleValue());
                            credit.setConsumptionAmount(updatedConsumptionAmount);
                            credit.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount));
                            return creditRepository.save(credit)
                                    .flatMap(savedAccount -> webClient.post(
                                            "/v1/transaction",
                                            transaction,
                                            TransactionResponse.class));
                        }));
    }

    @Override
    @Transactional
    public Mono<TransactionResponse> chargeCreditCard(String idCredit, Mono<ChargeRequest> chargeRequestMono) {
        TransactionRequest transaction = new TransactionRequest();
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND))))
                .flatMap(credit -> chargeRequestMono
                        .filter(isNotCreditLimitExceeded(credit))
                        .switchIfEmpty(Mono.error(new CustomException(CustomError.E_INSUFFICIENT_BALANCE)))
                        .flatMap(charge -> {
                            BigDecimal updatedConsumptionAmount = credit.getConsumptionAmount().add(charge.getAmount());
                            transaction.setProductId(idCredit);
                            transaction.setClientId(credit.getClientId());
                            transaction.setType(TransactionRequest.TypeEnum.CHARGE);
                            transaction.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount).doubleValue());
                            transaction.setAmount(charge.getAmount().doubleValue());
                            credit.setConsumptionAmount(updatedConsumptionAmount);
                            credit.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount));
                            return creditRepository.save(credit)
                                    .flatMap(savedAccount -> webClient.post(
                                            "/v1/transaction",
                                            transaction,
                                            TransactionResponse.class));
                        }));
    }

    private Predicate<ChargeRequest> isNotCreditLimitExceeded(Credit credit) {
        return charge -> charge.getAmount().compareTo(credit.getCreditLimit().subtract(credit.getConsumptionAmount())) <= 0;
    }

    @Override
    public Mono<BalanceResponse> getClientBalances(String idCredit) {
        return creditRepository.findById(idCredit)
                .map(CreditMapper.INSTANCE::toBalanceResponse);
    }

}
