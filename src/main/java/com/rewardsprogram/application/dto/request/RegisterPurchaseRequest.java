package com.rewardsprogram.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterPurchaseRequest(

        @NotBlank(message = "customerId no puede estar vacío")
        String customerId,

        @NotNull(message = "amount es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount debe ser mayor que 0")
        BigDecimal amount
) {
}
