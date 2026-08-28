package com.rewardsprogram.application.dto.response;

import java.math.BigDecimal;

public record RedemptionResponse(
        String customerId,
        Long redemptionId,
        long pointsRedeemed,
        BigDecimal amountValue,
        long remainingPoints
) {
}
