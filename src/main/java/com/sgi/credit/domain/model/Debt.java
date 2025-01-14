package com.sgi.credit.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a debts in the system.
 * Contains information about the debt cardId, debt amount, status
 * and clientId.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "debt")
public class Debt {

    @Id
    private String id;

    @Indexed
    private String creditId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private String status;

    @Indexed
    private String clientId;

    private Instant dueDate;
}
