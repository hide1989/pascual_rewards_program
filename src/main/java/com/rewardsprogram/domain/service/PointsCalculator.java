package com.rewardsprogram.domain.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PointsCalculator {

    private static final BigDecimal PESOS_PER_POINT = BigDecimal.valueOf(1000);
    private static final BigDecimal PESOS_VALUE_PER_POINT = BigDecimal.valueOf(100);

    public long pointsEarnedForPurchase(BigDecimal previousTotal, BigDecimal purchaseAmount) {
        BigDecimal newTotal = previousTotal.add(purchaseAmount);
        return totalPointsEarned(newTotal) - totalPointsEarned(previousTotal);
    }

    public long totalPointsEarned(BigDecimal totalAmountPurchased) {
        return totalAmountPurchased.divideToIntegralValue(PESOS_PER_POINT).longValueExact();
    }

    public BigDecimal pointsToPesos(long points) {
        return PESOS_VALUE_PER_POINT.multiply(BigDecimal.valueOf(points)).setScale(2, RoundingMode.UNNECESSARY);
    }
}
