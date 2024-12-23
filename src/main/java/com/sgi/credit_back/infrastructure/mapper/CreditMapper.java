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

@Mapper
public interface CreditMapper {

    CreditMapper INSTANCE = Mappers.getMapper(CreditMapper.class);

    @Mapping(target = "type", source = "type")
    CreditResponse map(Credit credit);

    @Mapping(target = "id", ignore = true)
    Credit map(CreditRequest creditRequest);

    @Mapping(target = "balance", source = "amount")
    BalanceResponse balance (Credit credit);

    default Mono<Credit> map(Mono<CreditRequest> creditRequestMono) {
        return creditRequestMono.map(this::map);
    }
}
