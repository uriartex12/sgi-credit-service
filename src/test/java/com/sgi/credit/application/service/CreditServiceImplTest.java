package com.sgi.credit.application.service;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.domain.ports.out.CreditRepository;
import com.sgi.credit.domain.ports.out.DebtRepository;
import com.sgi.credit.domain.ports.out.FeignExternalService;
import com.sgi.credit.helper.FactoryTest;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import com.sgi.credit.infrastructure.dto.TransactionRequest;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.mapper.CreditMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CreditServiceImpl} class.
 * This test class validates the behavior of the CreditService implementation
 * by mocking dependencies such as {@link CreditRepository} and {@link FeignExternalService}.
 * Reactive streams are verified using {@link StepVerifier}.
 */
@ExtendWith(MockitoExtension.class)
public class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private FeignExternalService feignExternalService;

    @InjectMocks
    private CreditServiceImpl creditService;

    @Mock
    private DebtRepository debtRepository;

    @Test
    void createCredit_shouldReturnCreatedResponse() {
        CreditRequest creditRequest = FactoryTest.toFactoryBankCredit(CreditRequest.class);
        CreditResponse creditResponse =  FactoryTest.toFactoryBankCredit(CreditResponse.class);
        DebtResponse debtResponse =  FactoryTest.toFactoryDebt(creditResponse.getId(),
                creditResponse.getClientId(), creditResponse.getConsumptionAmount());
        when(creditRepository.save(any(Credit.class)))
                .thenReturn(Mono.just(creditResponse));

        when(debtRepository.save(any(Debt.class)))
                .thenReturn(Mono.just(debtResponse));

        Mono<CreditResponse> result = creditService.createCredit(Mono.just(creditRequest));
        StepVerifier.create(result)
                .expectNext(creditResponse)
                .verifyComplete();
        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    void deleteCredit_shouldReturnVoid() {
        String creditId = UUID.randomUUID().toString();
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setId(creditId);
        when(creditRepository.findById(creditId)).thenReturn(Mono.just(credit));
        when(creditRepository.delete(credit)).thenReturn(Mono.empty());
        Mono<Void> result = creditService.deleteCredit(creditId);
        StepVerifier.create(result)
                .verifyComplete();
        verify(creditRepository).findById(creditId);
        verify(creditRepository).delete(credit);
    }

    @Test
    void deleteCredit_shouldReturnNotFound() {
        String creditId = UUID.randomUUID().toString();
        when(creditRepository.findById(creditId)).thenReturn(Mono.empty());
        Mono<Void> result = creditService.deleteCredit(creditId);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> true)
                .verify();

        verify(creditRepository).findById(creditId);
        verifyNoMoreInteractions(creditRepository);
    }

    @Test
    void getAllCredits_shouldReturnListCreditResponse() {

        List<CreditResponse> credits = FactoryTest.toFactoryListCredits();
        when(creditRepository.findAll(anyString(), anyString(), anyString())).thenReturn(Flux.fromIterable(credits));
        Flux<CreditResponse> result = creditService.getAllCredits(anyString(), anyString(), anyString());

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
        verify(creditRepository).findAll(anyString(), anyString(), anyString());
    }

    @Test
    void getCreditById_shouldReturnListCreditResponse() {
        String creditId = UUID.randomUUID().toString();
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setId(creditId);
        CreditResponse creditResponse = CreditMapper.INSTANCE.toCreditResponse(credit);
        when(creditRepository.findById(creditId)).thenReturn(Mono.just(credit));
        Mono<CreditResponse> result = creditService.getCreditById(creditId);
        StepVerifier.create(result)
                .expectNext(creditResponse)
                .verifyComplete();
        verify(creditRepository).findById(creditId);
    }

    @Test
    void updateCredit_shouldReturnCreditResponse() {
        String creditId = UUID.randomUUID().toString();
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setId(creditId);
        CreditRequest creditRequest = FactoryTest.toFactoryBankCredit(CreditRequest.class);
        CreditResponse creditResponse = CreditMapper.INSTANCE.toCreditResponse(credit);

        when(creditRepository.findById(creditId)).thenReturn(Mono.just(credit));
        when(creditRepository.save(credit)).thenReturn(Mono.just(creditResponse));
        Mono<CreditResponse> result = creditService.updateCredit(creditId, Mono.just(creditRequest));
        StepVerifier.create(result)
                .expectNext(creditResponse)
                .verifyComplete();
        verify(creditRepository).findById(creditId);
        verify(creditRepository).save(credit);
    }

    @Test
    void testGetCreditIdTransactions_Success() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        TransactionResponse transactionResponse = FactoryTest.toFactoryTransactionResponse(credit.getId());
        transactionResponse.setClientId(credit.getClientId());

        when(creditRepository.findById(credit.getId())).thenReturn(Mono.just(credit));
        when(feignExternalService.get(anyString(), anyString(), eq(TransactionResponse.class)))
                .thenReturn(Flux.just(transactionResponse));

        Flux<TransactionResponse> result = creditService.getClientTransactions(credit.getId());

        StepVerifier.create(result)
                .expectNext(transactionResponse)
                .verifyComplete();

        verify(creditRepository).findById(credit.getId());
        verify(feignExternalService).get(anyString(), anyString(), eq(TransactionResponse.class));
    }

    @Test
    void testMakePayment_Success() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setConsumptionAmount(BigDecimal.valueOf(100));
        credit.setCreditLimit(BigDecimal.valueOf(200));
        TransactionResponse transactionResponse = FactoryTest.toFactoryTransactionResponse(credit.getId());
        transactionResponse.setClientId(credit.getClientId());
        PaymentRequest paymentRequest = FactoryTest.toFactoryPaymentRequest();
        when(creditRepository.findById(credit.getId())).thenReturn(Mono.just(credit));
        when(creditRepository.save(any(Credit.class))).thenReturn(Mono.just(CreditMapper.INSTANCE.toCreditResponse(credit)));
        when(feignExternalService.post(anyString(), any(TransactionRequest.class), eq(TransactionResponse.class)))
                .thenReturn(Mono.just(transactionResponse));

        Mono<TransactionResponse> result = creditService.makePayment(credit.getId(), Mono.just(paymentRequest));

        StepVerifier.create(result)
                .expectNext(transactionResponse)
                .verifyComplete();
        verify(creditRepository).findById(credit.getId());
        verify(creditRepository).save(any(Credit.class));
        verify(feignExternalService).post(anyString(), any(TransactionRequest.class), eq(TransactionResponse.class));
    }

    @Test
    void testChargeCredit_Success() {
        Credit credit = FactoryTest.toFactoryEntityCredit();
        credit.setConsumptionAmount(BigDecimal.valueOf(100));
        credit.setCreditLimit(BigDecimal.valueOf(200));
        TransactionResponse transactionResponse = FactoryTest.toFactoryTransactionResponse(credit.getId());
        transactionResponse.setClientId(credit.getClientId());
        ChargeRequest chargeRequest = FactoryTest.toFactoryChargeRequest();
        when(creditRepository.findById(credit.getId())).thenReturn(Mono.just(credit));
        when(creditRepository.save(any(Credit.class))).thenReturn(Mono.just(CreditMapper.INSTANCE.toCreditResponse(credit)));
        when(feignExternalService.post(anyString(), any(TransactionRequest.class), eq(TransactionResponse.class)))
                .thenReturn(Mono.just(transactionResponse));

        Mono<TransactionResponse> result = creditService.chargeCreditCard(credit.getId(), Mono.just(chargeRequest));

        StepVerifier.create(result)
                .expectNext(transactionResponse)
                .verifyComplete();
        verify(creditRepository).findById(credit.getId());
        verify(creditRepository).save(any(Credit.class));
        verify(feignExternalService).post(anyString(), any(TransactionRequest.class), eq(TransactionResponse.class));
    }





}
