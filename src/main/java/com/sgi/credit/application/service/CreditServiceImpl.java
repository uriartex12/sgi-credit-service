package com.sgi.credit.application.service;

import java.math.BigDecimal;
import com.sgi.credit.domain.model.Credit;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Predicate;

import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.domain.ports.out.DebtRepository;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.DebtRequest;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import com.sgi.credit.infrastructure.dto.TransactionRequest;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.mapper.CreditMapper;
import com.sgi.credit.infrastructure.mapper.DebtMapper;
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

import static com.sgi.credit.domain.shared.Constants.generateAccountNumber;

/**
 * Service implementation for managing credits.
 */
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final FeignExternalService webClient;
    private final DebtRepository debtRepository;


    @Override
    public Mono<CreditResponse> createCredit(Mono<CreditRequest> credit) {
        return credit.flatMap(creditMono ->
                hasOverdueDebt(creditMono.getClientId())
                        .filter(hasOverdue -> !hasOverdue)
                        .switchIfEmpty(Mono.error(new CustomException(CustomError.E_OUTSTANDING_DEBT)))
                        .flatMap(ignored -> {
                            Credit creditBank = CreditMapper.INSTANCE.toCredit(creditMono, generateAccountNumber());
                            return creditRepository.save(creditBank)
                                    .flatMap(creditResponse -> {
                                        Debt debtRequest = DebtMapper.INSTANCE.toDebtRequest(creditResponse,
                                                DebtRequest.StatusEnum.ACTIVE.name());
                                        return debtRepository.save(debtRequest)
                                                .thenReturn(creditResponse);
                                    });
                        })
        );
    }

    /**
     * Checks if the client's debt is overdue.
     * Compares the due date of the debt with the current date. Returns true if the debt is overdue,
     * meaning the due date is before the first day of the current month.
     *
     * @param clientId The client's ID.
     * @return A Mono emitting true if the debt is overdue, false otherwise.
     */
    public Mono<Boolean> hasOverdueDebt(String clientId) {
        return debtRepository.findByClientIdAndStatus(clientId, DebtRequest.StatusEnum.ACTIVE.name())
                .map(debt -> {
                    LocalDate dueDate = debt.getDueDate().atZone(ZoneId.systemDefault()).toLocalDate();
                    return dueDate.isBefore(LocalDate.now().withDayOfMonth(1));
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> deleteCredit(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND)))
                .flatMap(creditRepository::delete);
    }

    @Override
    public Flux<CreditResponse> getAllCredits(String creditId, String type, String clientId) {
        return creditRepository.findAll(creditId, type, clientId);
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
                        "/v1/transactions/{productId}/card",
                        idCredit,
                        TransactionResponse.class));
    }

    @Override
    @Transactional
    public Mono<TransactionResponse> makePayment(String idCredit, Mono<PaymentRequest> paymentRequestMono) {
        TransactionRequest transaction = new TransactionRequest();
        return creditRepository.findById(idCredit)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CREDIT_NOT_FOUND)))
                .flatMap(credit ->
                        paymentRequestMono
                                .filter(payment -> payment.getAmount().compareTo(credit.getConsumptionAmount()) <= 0)
                                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_INVALID_INPUT)))
                                .flatMap(payment -> {
                                    BigDecimal updatedConsumptionAmount = credit.getConsumptionAmount()
                                            .subtract(payment.getAmount());
                                    return debtRepository.findByClientIdAndStatus(credit.getClientId(),
                                                    DebtRequest.StatusEnum.ACTIVE.name())
                                            .flatMap(debt -> {
                                                debt.setAmount(updatedConsumptionAmount);
                                                if (debt.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                                                    debt.setStatus(DebtRequest.StatusEnum.PAID.name());
                                                    return debtRepository.save(debt)
                                                            .flatMap(this::createNewDebtForNextMonth);
                                                }
                                                return debtRepository.save(debt);
                                            })
                                            .then(Mono.defer(() -> {
                                                transaction.setProductId(idCredit);
                                                transaction.setClientId(credit.getClientId());
                                                transaction.setType(TransactionRequest.TypeEnum.PAYMENT);
                                                transaction.setBalance(credit.getCreditLimit()
                                                        .subtract(updatedConsumptionAmount).doubleValue());
                                                transaction.setAmount(payment.getAmount().doubleValue());
                                                credit.setConsumptionAmount(updatedConsumptionAmount);
                                                credit.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount));
                                                return creditRepository.save(credit)
                                                        .flatMap(savedCredit ->
                                                                webClient.post("/v1/transactions", transaction,
                                                                        TransactionResponse.class)
                                                        );
                                            }));
                                })
                );
    }

    private Mono<Debt> createNewDebtForNextMonth(DebtResponse currentDebt) {
        Debt newDebt = new Debt();
        newDebt.setCreditId(currentDebt.getCreditId());
        newDebt.setClientId(currentDebt.getClientId());
        newDebt.setAmount(BigDecimal.ZERO);
        newDebt.setStatus(DebtRequest.StatusEnum.ACTIVE.name());
        newDebt.setDueDate(currentDebt.getDueDate().plusNanos(1).toInstant());
        return debtRepository.save(newDebt)
                .map(debt ->  DebtMapper.INSTANCE.convertDebtResponseToDebt(Mono.just(debt)));
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
                            return debtRepository.findByClientIdAndStatus(credit.getClientId(),
                                            DebtRequest.StatusEnum.ACTIVE.name())
                                    .flatMap(debt -> {
                                        debt.setAmount(updatedConsumptionAmount);
                                        return debtRepository.save(debt);
                                    })
                                    .then(Mono.defer(() -> {
                                        transaction.setProductId(idCredit);
                                        transaction.setClientId(credit.getClientId());
                                        transaction.setType(TransactionRequest.TypeEnum.CHARGE);
                                        transaction.setBalance(credit.getCreditLimit()
                                                .subtract(updatedConsumptionAmount).doubleValue());
                                        transaction.setAmount(charge.getAmount().doubleValue());
                                        credit.setConsumptionAmount(updatedConsumptionAmount);
                                        credit.setBalance(credit.getCreditLimit().subtract(updatedConsumptionAmount));
                                        return creditRepository.save(credit)
                                                .flatMap(savedAccount -> webClient.post(
                                                        "/v1/transactions",
                                                        transaction,
                                                        TransactionResponse.class));
                                            }));


                        }));
    }

    @Override
    public Flux<CreditResponse> getCreditCardByClientId(String clientId) {
        return creditRepository.getCreditCardByClientId(clientId);
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
