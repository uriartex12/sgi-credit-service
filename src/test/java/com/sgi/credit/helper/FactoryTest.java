package com.sgi.credit.helper;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.ChargeRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import com.sgi.credit.infrastructure.dto.PaymentRequest;
import com.sgi.credit.infrastructure.dto.TransactionResponse;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Class containing methods to generate CreditRequest and CreditResponse objects
 * with default values to facilitate unit testing.
 */
public class FactoryTest {

    /**
     * Generates an CreditRequest object with default values for testing.
     *
     * @return An CreditRequest object with configured values for testing.
     */
    @SneakyThrows
    public static <R> R toFactoryBankCredit(Class<R> response) {
        R credit = response.getDeclaredConstructor().newInstance();
        if (credit instanceof CreditRequest) {
            return (R) initializeCredit((CreditRequest) credit);
        } else if (credit instanceof CreditResponse) {
            return (R) initializeCredit((CreditResponse) credit);
        }
        return credit;
    }

    private static CreditRequest initializeCredit(CreditRequest credit) {
        credit.setClientId("client-test-0001");
        credit.setType(CreditRequest.TypeEnum.PERSONAL);
        credit.setCreditLimit(BigDecimal.valueOf(10));
        credit.setInterestRate(BigDecimal.valueOf(10));
        return credit;
    }

    private static CreditResponse initializeCredit(CreditResponse credit) {
        credit.setId(randomUUID().toString());
        credit.setClientId("client-test-0001");
        credit.balance(BigDecimal.ZERO);
        credit.setCreditNumber(UUID.randomUUID().toString());
        credit.setConsumptionAmount(BigDecimal.valueOf(1));
        credit.setType(CreditResponse.TypeEnum.PERSONAL);
        credit.setCreditLimit(BigDecimal.valueOf(1));
        return credit;
    }

    /**
     * Generates DebtRequest objects with predefined values for testing purposes.
     *
     * @return DebtRequest objects with default data.
     */
    public static DebtResponse toFactoryDebt(String cardId, String clientId, BigDecimal amount) {
        DebtResponse debtResponse = new DebtResponse();
        debtResponse.setStatus(DebtResponse.StatusEnum.ACTIVE);
        debtResponse.setCardId(cardId);
        debtResponse.setClientId(clientId);
        debtResponse.setAmount(amount);
        return  debtResponse;
    }

    /**
     * Generates a list of CreditResponse objects with predefined values for testing purposes.
     *
     * @return A list of CreditResponse objects with default data.
     */
    public static List<CreditResponse> toFactoryListCredits() {
        CreditResponse creditOne = new CreditResponse();
        creditOne.setId(randomUUID().toString());
        creditOne.setClientId("client-test-0001");
        creditOne.balance(BigDecimal.ZERO);
        creditOne.setCreditNumber(UUID.randomUUID().toString());
        creditOne.setConsumptionAmount(BigDecimal.valueOf(1));
        creditOne.setType(CreditResponse.TypeEnum.PERSONAL);
        creditOne.setCreditLimit(BigDecimal.valueOf(1));

        CreditResponse creditTwo = new CreditResponse();
        creditTwo.setId(randomUUID().toString());
        creditTwo.setClientId("client-test-0001");
        creditTwo.balance(BigDecimal.ZERO);
        creditTwo.setCreditNumber(UUID.randomUUID().toString());
        creditTwo.setConsumptionAmount(BigDecimal.valueOf(1));
        creditTwo.setType(CreditResponse.TypeEnum.PERSONAL);
        creditTwo.setCreditLimit(BigDecimal.valueOf(1));
        return List.of(creditOne, creditTwo);

    }
    /**
     * Creates a factory balance response with a default credit balance and a random client ID.
     *
     * @return a {@link BalanceResponse} instance with predefined values.
     */
    public static BalanceResponse toFactoryBalanceClient() {
        BalanceResponse balance = new BalanceResponse();
        balance.setBalance(BigDecimal.valueOf(1000));
        balance.setClientId(randomUUID().toString());
        return balance;
    }

    /**
     * Creates a payment request with a default amount.
     *
     * @return A PaymentRequest instance with a preset deposit amount.
     */
    public static PaymentRequest toFactoryPaymentRequest() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(BigDecimal.valueOf(10));
        return paymentRequest;
    }

    /**
     * Creates a payment request with a default amount.
     *
     * @return A PaymentRequest instance with a preset deposit amount.
     */
    public static ChargeRequest toFactoryChargeRequest() {
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setAmount(BigDecimal.valueOf(10));
        return chargeRequest;
    }

    /**
     * Creates a factory transaction response with a default client ID, a random product ID, and a fixed deposit amount.
     *
     * @param productId the product ID for the transaction.
     * @return a {@link TransactionResponse} instance with predefined values.
     */
    public static TransactionResponse toFactoryTransactionResponse(String productId) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setClientId(randomUUID().toString());
        transactionResponse.setAmount(BigDecimal.valueOf(100));
        transactionResponse.setType(TransactionResponse.TypeEnum.PAYMENT);
        transactionResponse.setProductId(productId);
        return transactionResponse;
    }

    /**
     * Creates a list of TransactionResponse objects with default values for testing purposes.
     *
     * @param productId The product ID to associate with the transaction response.
     * @return A list containing a single TransactionResponse object with preset values.
     */
    public static List<TransactionResponse> toFactoryListTransactionResponse(String productId) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setClientId(randomUUID().toString());
        transactionResponse.setAmount(BigDecimal.valueOf(100));
        transactionResponse.setType(TransactionResponse.TypeEnum.CHARGE);
        transactionResponse.setProductId(productId);
        return List.of(transactionResponse);
    }

    /**
     * Creates a new Credit object with predefined values for testing purposes.
     *
     * @return A Credit object populated with random and default values, such as a client ID, balance,
     *         credit type, and other attributes.
     */
    public static Credit toFactoryEntityCredit() {
        return Credit.builder()
                .id(randomUUID().toString())
                .createdDate(Instant.now())
                .interestRate(BigDecimal.valueOf(10))
                .balance(BigDecimal.valueOf(2000))
                .consumptionAmount(BigDecimal.ZERO)
                .clientId("client-test-0001")
                .createdDate(Instant.now())
                .type("PERSONAL")
                .build();
    }



}
