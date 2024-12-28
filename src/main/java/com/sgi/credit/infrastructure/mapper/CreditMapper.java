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
 * Mapper para la conversión entre objetos Credit y otros DTOs como CreditRequest, CreditResponse y BalanceResponse.
 * Utiliza MapStruct para automatizar la conversión de tipos entre objetos.
 */
@Mapper
public interface CreditMapper {

    /**
     * Instancia del mapper.
     */
    CreditMapper INSTANCE = Mappers.getMapper(CreditMapper.class);

    /**
     * Convierte un objeto Credit a CreditResponse.
     *
     * @param credit El objeto Credit.
     * @return El objeto CreditResponse.
     */
    @Mapping(target = "type", source = "type")
    CreditResponse toCreditResponse(Credit credit);

    /**
     * Convierte un objeto CreditRequest a Credit, ignorando el campo id.
     *
     * @param creditRequest El objeto CreditRequest.
     * @param accountNumber El número de cuenta.
     * @return El objeto Credit.
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

    /**
     * Convierte un objeto Credit a BalanceResponse.
     *
     * @param credit El objeto Credit.
     * @return El objeto BalanceResponse.
     */
    @Mapping(target = "balance", source = "balance")
    BalanceResponse toBalanceResponse(Credit credit);

    /**
     * Convierte un objeto Instant a OffsetDateTime.
     *
     * @param instant El objeto Instant.
     * @return El objeto OffsetDateTime.
     */
    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
