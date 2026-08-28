package com.rewardsprogram.application.dto.response;

import java.math.BigDecimal;

public record PurchaseResponse(
        String customerId,
        Long purchaseId,
        BigDecimal amount,
        long pointsEarned,
        long totalAvailablePoints
) {
}
