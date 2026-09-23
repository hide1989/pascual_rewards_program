package com.rewardsprogram.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PointsCalculatorTest {

    private final PointsCalculator pointsCalculator = new PointsCalculator();

    @Test
    void pointsEarnedForPurchase_shouldReturnZero_whenPurchaseDoesNotCompleteAThousand() {
        // Arrange
        BigDecimal previousTotal = BigDecimal.ZERO;
        BigDecimal purchaseAmount = BigDecimal.valueOf(700);

        // Act
        long pointsEarned = pointsCalculator.pointsEarnedForPurchase(previousTotal, purchaseAmount);

        // Assert
        assertThat(pointsEarned).isZero();
    }

    @Test
    void pointsEarnedForPurchase_shouldReturnCarriedOverPoint_whenRemainderFromPreviousPurchaseCompletesAThousand() {
        // Arrange
        BigDecimal previousTotal = BigDecimal.valueOf(700);
        BigDecimal purchaseAmount = BigDecimal.valueOf(500);

        // Act
        long pointsEarned = pointsCalculator.pointsEarnedForPurchase(previousTotal, purchaseAmount);

        // Assert
        assertThat(pointsEarned).isEqualTo(1);
    }

    @Test
    void pointsEarnedForPurchase_shouldReturnExactPoints_whenAmountIsAnExactMultipleOfAThousand() {
        // Arrange
        BigDecimal previousTotal = BigDecimal.ZERO;
        BigDecimal purchaseAmount = BigDecimal.valueOf(3000);

        // Act
        long pointsEarned = pointsCalculator.pointsEarnedForPurchase(previousTotal, purchaseAmount);

        // Assert
        assertThat(pointsEarned).isEqualTo(3);
    }

    @Test
    void totalPointsEarned_shouldFloorDivideByAThousand() {
        // Arrange
        BigDecimal totalAmountPurchased = BigDecimal.valueOf(2999);

        // Act
        long totalPoints = pointsCalculator.totalPointsEarned(totalAmountPurchased);

        // Assert
        assertThat(totalPoints).isEqualTo(2);
    }

    @Test
    void totalPointsEarned_shouldReturnZero_whenTotalIsBelowOneThousand() {
        // Arrange
        BigDecimal totalAmountPurchased = BigDecimal.valueOf(999);

        // Act
        long totalPoints = pointsCalculator.totalPointsEarned(totalAmountPurchased);

        // Assert
        assertThat(totalPoints).isZero();
    }

    @Test
    void pointsToPesos_shouldMultiplyPointsByOneHundred() {
        // Arrange
        long points = 5L;

        // Act
        BigDecimal pesos = pointsCalculator.pointsToPesos(points);

        // Assert
        assertThat(pesos).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void pointsToPesos_shouldReturnZero_whenPointsIsZero() {
        // Arrange
        long points = 0L;

        // Act
        BigDecimal pesos = pointsCalculator.pointsToPesos(points);

        // Assert
        assertThat(pesos).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
