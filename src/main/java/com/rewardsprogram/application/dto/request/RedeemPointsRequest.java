package com.rewardsprogram.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RedeemPointsRequest(

        @NotBlank(message = "customerId no puede estar vacío")
        String customerId,

        @NotNull(message = "points es obligatorio")
        @Positive(message = "points debe ser mayor que 0")
        Long points
) {
}
