package com.rewardsprogram.application.dto.response;

public record CustomerPointsBalanceResponse(
        String customerId,
        long totalPointsEarned,
        long totalPointsRedeemed,
        long availablePoints
) {
}
