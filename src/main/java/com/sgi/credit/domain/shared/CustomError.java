package com.sgi.credit.domain.shared;

import com.sgi.credit.infrastructure.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum representing custom errors for the Credit-service application.
 * Each constant includes an error code, message, and HTTP status for specific errors.
 */
@Getter
@AllArgsConstructor
public enum CustomError {

    E_OPERATION_FAILED(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "CREDIT-000", "Operation External failed")),
    E_INVALID_INPUT(new ApiError(HttpStatus.BAD_REQUEST, "CREDIT-100", "Invalid input provided")),
    E_CREDIT_NOT_FOUND(new ApiError(HttpStatus.NOT_FOUND, "CREDIT-001", "Bank credit not found")),
    E_OUTSTANDING_DEBT(new ApiError(HttpStatus.CONFLICT, "CREDIT-006", "The client has an outstanding debt.")),
    E_INSUFFICIENT_BALANCE(new ApiError(HttpStatus.PAYMENT_REQUIRED, "CREDIT-004", "Insufficient balance"));

    private final ApiError error;
}

