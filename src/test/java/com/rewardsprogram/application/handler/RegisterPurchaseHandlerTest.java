package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.command.RegisterPurchaseCommand;
import com.rewardsprogram.application.dto.response.PurchaseResponse;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.model.Purchase;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import com.rewardsprogram.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterPurchaseHandlerTest {

    @Mock
    private CustomerPointsAccountRepository accountRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PointsCalculator pointsCalculator;

    private RegisterPurchaseHandler handler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        handler = new RegisterPurchaseHandler(accountRepository, purchaseRepository, pointsCalculator);
    }

    @Test
    void handle_shouldCreateNewAccount_whenCustomerHasNoPreviousPurchases() {
        // Arrange
        String customerId = "cust-1";
        BigDecimal amount = BigDecimal.valueOf(1500);
        RegisterPurchaseCommand command = new RegisterPurchaseCommand(customerId, amount);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(pointsCalculator.pointsEarnedForPurchase(BigDecimal.ZERO, amount)).thenReturn(1L);
        when(pointsCalculator.totalPointsEarned(amount)).thenReturn(1L);
        when(accountRepository.save(any(CustomerPointsAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Purchase savedPurchase = new Purchase(customerId, amount, 1L, amount);
        ReflectionTestUtils.setField(savedPurchase, "id", 10L);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedPurchase);

        // Act
        PurchaseResponse response = handler.handle(command);

        // Assert
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.purchaseId()).isEqualTo(10L);
        assertThat(response.amount()).isEqualByComparingTo(amount);
        assertThat(response.pointsEarned()).isEqualTo(1L);
        assertThat(response.totalAvailablePoints()).isEqualTo(1L);

        ArgumentCaptor<CustomerPointsAccount> accountCaptor = ArgumentCaptor.forClass(CustomerPointsAccount.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getCustomerId()).isEqualTo(customerId);
        assertThat(accountCaptor.getValue().getTotalAmountPurchased()).isEqualByComparingTo(amount);
    }

    @Test
    void handle_shouldAccumulateCarryOverAndSubtractRedeemedPoints_whenCustomerAlreadyHasAnAccount() {
        // Arrange
        String customerId = "cust-2";
        BigDecimal previousTotal = BigDecimal.valueOf(700);
        BigDecimal amount = BigDecimal.valueOf(500);
        BigDecimal newTotal = BigDecimal.valueOf(1200);

        CustomerPointsAccount existingAccount = new CustomerPointsAccount(customerId);
        existingAccount.setTotalAmountPurchased(previousTotal);
        existingAccount.setTotalPointsRedeemed(1L);

        RegisterPurchaseCommand command = new RegisterPurchaseCommand(customerId, amount);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingAccount));
        when(pointsCalculator.pointsEarnedForPurchase(previousTotal, amount)).thenReturn(1L);
        when(pointsCalculator.totalPointsEarned(newTotal)).thenReturn(1L);
        when(accountRepository.save(any(CustomerPointsAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Purchase savedPurchase = new Purchase(customerId, amount, 1L, newTotal);
        ReflectionTestUtils.setField(savedPurchase, "id", 11L);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(savedPurchase);

        // Act
        PurchaseResponse response = handler.handle(command);

        // Assert
        assertThat(response.pointsEarned()).isEqualTo(1L);
        // total points earned (1) minus the point already redeemed (1) = 0 available
        assertThat(response.totalAvailablePoints()).isEqualTo(0L);
        assertThat(existingAccount.getTotalAmountPurchased()).isEqualByComparingTo(newTotal);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        assertThat(purchaseCaptor.getValue().getAmount()).isEqualByComparingTo(amount);
        assertThat(purchaseCaptor.getValue().getTotalAmountAfterPurchase()).isEqualByComparingTo(newTotal);
    }
}
