package com.sgi.credit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a credit in the system.
 * Contains information about the credit number, consumed amount, balance,
 * interest rate, credit limit, and creation and modification dates.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "credit")
public class Credit {

    @Id
    private String id;
    private String creditNumber;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal consumptionAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal interestRate;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal creditLimit;

    private String type;
    private String clientId;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant updatedDate;
}
