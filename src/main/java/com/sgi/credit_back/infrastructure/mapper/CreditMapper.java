package com.sgi.credit_back.infrastructure.mapper;

import com.sgi.bank_account_back.infrastructure.dto.BalanceResponse;
import com.sgi.bank_account_back.infrastructure.dto.CreditRequest;
import com.sgi.bank_account_back.infrastructure.dto.CreditResponse;
import com.sgi.credit_back.domain.model.Credit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper
public interface CreditMapper {

    CreditMapper INSTANCE = Mappers.getMapper(CreditMapper.class);

    @Mapping(target = "type", source = "type")
    CreditResponse map(Credit credit);

    @Mapping(target = "id", ignore = true)
    default Credit map(CreditRequest creditRequest, String accountNumber) {
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
    BalanceResponse balance (Credit credit);


    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }

}
