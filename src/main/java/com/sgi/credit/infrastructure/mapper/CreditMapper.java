package com.sgi.credit.infrastructure.mapper;

import com.sgi.credit.domain.model.Credit;
import com.sgi.credit.infrastructure.dto.BalanceResponse;
import com.sgi.credit.infrastructure.dto.CreditRequest;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between Credit objects and other DTOs such as CreditRequest, CreditResponse, and BalanceResponse.
 * Uses MapStruct to automate the type conversion between objects.
 */
@Mapper
public interface CreditMapper {

    CreditMapper INSTANCE = Mappers.getMapper(CreditMapper.class);

    @Mapping(target = "type", source = "type")
    CreditResponse toCreditResponse(Credit credit);

    /**
     * Converts a CreditRequest to a Credit object.
     * This method also sets the credit number based on the provided account number
     * and initializes default values for the consumption amount, created date,
     * and updated date.
     *
     * @param creditRequest The CreditRequest object containing the details to create the Credit.
     * @param accountNumber The account number to be assigned to the credit.
     * @return The Credit object created from the CreditRequest.
     */
    @Mapping(target = "id", ignore = true)
    default Credit toCredit(CreditRequest creditRequest, String accountNumber) {
        return Credit.builder()
                .consumptionAmount(BigDecimal.ZERO)
                .creditNumber(accountNumber)
                .type(creditRequest.getType().getValue())
                .interestRate(creditRequest.getInterestRate())
                .creditLimit(creditRequest.getCreditLimit())
                .balance(creditRequest.getCreditLimit())
                .clientId(creditRequest.getClientId())
                .createdDate(Instant.now())
                .updatedDate(Instant.now())
                .build();
    }

    @Mapping(target = "balance", source = "balance")
    BalanceResponse toBalanceResponse(Credit credit);

    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
