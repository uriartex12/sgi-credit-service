package com.sgi.credit_back.domain.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

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
