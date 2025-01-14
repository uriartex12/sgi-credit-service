package com.sgi.credit.infrastructure.mapper;

import com.sgi.credit.domain.model.Debt;
import com.sgi.credit.infrastructure.dto.CreditResponse;
import com.sgi.credit.infrastructure.dto.DebtRequest;
import com.sgi.credit.infrastructure.dto.DebtResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


/**
 * Mapper for converting between Credit objects and other DTOs such as DebtRequest, DebtResponse.
 * Uses MapStruct to automate the type conversion between objects.
 */
@Mapper
public interface DebtMapper {

    DebtMapper INSTANCE = Mappers.getMapper(DebtMapper.class);

    DebtResponse toDebtResponse(Debt debt);

    Debt toDebt(Mono<DebtRequest> debtRequest);

    @Mapping(target = "creditId", source = "creditResponse.id")
    @Mapping(target = "amount", source = "creditResponse.consumptionAmount")
    @Mapping(target = "dueDate", expression = "java(java.time.LocalDate.now())")
    Debt toDebtRequest(CreditResponse creditResponse, String status);

    Debt convertDebtResponseToDebt(Mono<DebtResponse> debtResponse);

    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }


}
