package com.sgi.credit.infrastructure.controller;

import com.sgi.credit.application.service.CreditServiceImpl;
import com.sgi.credit.helper.FactoryTest;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

/**
 * Test suite for {@link CreditController}.
 * Verifies the correct behavior of API endpoints for credit operations.
 * Uses {@link WebTestClient} for simulating HTTP requests and responses.
 */
@WebFluxTest(controllers = CreditController.class)
public class CreditControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CreditServiceImpl creditService;


    @Test
    void createCredit_shouldReturnCreatedResponse() {
        CreditResponse creditResponse = FactoryTest.toFactoryBankCredit(CreditResponse.class);
        Mockito.when(creditService.createCredit(any(Mono.class)))
                .thenReturn(Mono.just(creditResponse));
        webTestClient.post()
                .uri("/v1/credit")
                .bodyValue(FactoryTest.toFactoryBankCredit(CreditRequest.class))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreditResponse.class)
                .consumeWith(creditResponseEntityExchangeResult -> {
                    CreditResponse actual = creditResponseEntityExchangeResult.getResponseBody();
                    Assertions.assertNotNull(Objects.requireNonNull(actual).getId());
                    Assertions.assertNotNull(actual.getCreditLimit());
                    Assertions.assertNull(actual.getCreatedDate());
                    Assertions.assertEquals(CreditResponse.TypeEnum.PERSONAL, actual.getType());
                })
                .returnResult();
        Mockito.verify(creditService, times(1)).createCredit(any(Mono.class));
    }

    @Test
    void deleteCredit_shouldReturnOkResponse() {
        String creditId = randomUUID().toString();
        Mockito.when(creditService.deleteCredit(creditId)).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri("/v1/credit/{id}", creditId)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void getAccountById_shouldReturnAccountResponse() {
        CreditResponse creditResponse = FactoryTest.toFactoryBankCredit(CreditResponse.class);
        Mockito.when(creditService.getCreditById(creditResponse.getId()))
                .thenReturn(Mono.just(creditResponse));
        webTestClient.get()
                .uri("/v1/credit/{creditId}", creditResponse.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CreditResponse.class)
                .consumeWith(System.out::println)
                .value(actual -> {
                    Assertions.assertEquals(creditResponse.getId(), actual.getId());
                    Assertions.assertEquals(creditResponse.getClientId(), actual.getClientId());
                });
    }

    @Test
    void getAllCredits_shouldReturnFluxOfCreditResponse() {
        List<CreditResponse> credits =  FactoryTest.toFactoryListCredits();
        Flux<CreditResponse> creditResponseFlux = Flux.fromIterable(credits);
        Mockito.when(creditService.getAllCredits()).thenReturn(creditResponseFlux);
        webTestClient.get()
                .uri("/v1/credit")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CreditResponse.class)
                .value(list -> assertThat(list).hasSize(2));
    }

    @Test
    void getClientCreditBalances_shouldReturnBalanceResponse() {
        String creditId = randomUUID().toString();
        BalanceResponse balanceResponse = FactoryTest.toFactoryBalanceClient();
        Mockito.when(creditService.getClientBalances(creditId)).thenReturn(Mono.just(balanceResponse));
        webTestClient.get()
                .uri("/v1/credit/{creditId}/balances", creditId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BalanceResponse.class)
                .isEqualTo(balanceResponse);
        Mockito.verify(creditService, times(1)).getClientBalances(creditId);
    }

    @Test
    void makePayment_shouldReturnTransactionResponse() {
        String creditId = randomUUID().toString();
        PaymentRequest paymentRequest = FactoryTest.toFactoryPaymentRequest();
        TransactionResponse transactionResponse = FactoryTest.toFactoryTransactionResponse(creditId);

        Mockito.when(creditService.makePayment(eq(creditId), any(Mono.class)))
                .thenReturn(Mono.just(transactionResponse));

        webTestClient.post()
                .uri("/v1/credit/{creditId}/payment", creditId)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TransactionResponse.class)
                .isEqualTo(transactionResponse);

        Mockito.verify(creditService, times(1)).makePayment(eq(creditId), any(Mono.class));
    }

    @Test
    void chargeCreditCard_shouldReturnTransactionResponse() {
        String creditId = randomUUID().toString();
        ChargeRequest chargeRequest = FactoryTest.toFactoryChargeRequest();
        TransactionResponse transactionResponse = FactoryTest.toFactoryTransactionResponse(creditId);

        Mockito.when(creditService.chargeCreditCard(eq(creditId), any(Mono.class)))
                .thenReturn(Mono.just(transactionResponse));

        webTestClient.post()
                .uri("/v1/credit/{creditId}/charge", creditId)
                .bodyValue(chargeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TransactionResponse.class)
                .isEqualTo(transactionResponse);

        Mockito.verify(creditService, times(1)).chargeCreditCard(eq(creditId), any(Mono.class));
    }

    @Test
    void getClientTransactions_shouldReturnTransactionResponse() {
        String creditId = randomUUID().toString();
        List<TransactionResponse> transactionResponse = FactoryTest.toFactoryListTransactionResponse(creditId);
        Mockito.when(creditService.getClientTransactions(eq(creditId)))
                .thenReturn(Flux.fromIterable(transactionResponse));

        webTestClient.get()
                .uri("/v1/credit/{creditId}/transactions", creditId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(TransactionResponse.class)
                .isEqualTo(transactionResponse);

        Mockito.verify(creditService, times(1)).getClientTransactions(eq(creditId));
    }

    @Test
    void updateCredit_shouldReturnTransactionResponse() {
        String creditId = randomUUID().toString();
        CreditRequest creditRequest = FactoryTest.toFactoryBankCredit(CreditRequest.class);
        CreditResponse creditResponse = FactoryTest.toFactoryBankCredit(CreditResponse.class);
        creditResponse.setId(creditId);

        Mockito.when(creditService.updateCredit(eq(creditId), any(Mono.class)))
                .thenReturn(Mono.just(creditResponse));

        webTestClient.put()
                .uri("/v1/credit/{creditId}", creditId)
                .bodyValue(creditRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CreditResponse.class);

        Mockito.verify(creditService, times(1)).updateCredit(eq(creditId), any(Mono.class));
    }


}
