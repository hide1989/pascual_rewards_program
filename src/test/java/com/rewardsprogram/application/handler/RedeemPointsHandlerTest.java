package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.command.RedeemPointsCommand;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.model.Redemption;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.exception.InsufficientPointsException;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import com.rewardsprogram.repository.RedemptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedeemPointsHandlerTest {

    @Mock
    private CustomerPointsAccountRepository accountRepository;

    @Mock
    private RedemptionRepository redemptionRepository;

    @Mock
    private PointsCalculator pointsCalculator;

    private RedeemPointsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RedeemPointsHandler(accountRepository, redemptionRepository, pointsCalculator);
    }

    @Test
    void handle_shouldRedeemPoints_whenCustomerHasSufficientBalance() {
        // Arrange
        String customerId = "cust-1";
        BigDecimal totalAmountPurchased = BigDecimal.valueOf(5000);
        CustomerPointsAccount account = new CustomerPointsAccount(customerId);
        account.setTotalAmountPurchased(totalAmountPurchased);
        account.setTotalPointsRedeemed(1L);

        RedeemPointsCommand command = new RedeemPointsCommand(customerId, 2L);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));
        when(pointsCalculator.totalPointsEarned(totalAmountPurchased)).thenReturn(5L);
        when(pointsCalculator.pointsToPesos(2L)).thenReturn(BigDecimal.valueOf(200));
        when(accountRepository.save(any(CustomerPointsAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Redemption savedRedemption = new Redemption(customerId, 2L, BigDecimal.valueOf(200));
        ReflectionTestUtils.setField(savedRedemption, "id", 7L);
        when(redemptionRepository.save(any(Redemption.class))).thenReturn(savedRedemption);

        // Act
        RedemptionResponse response = handler.handle(command);

        // Assert
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.redemptionId()).isEqualTo(7L);
        assertThat(response.pointsRedeemed()).isEqualTo(2L);
        assertThat(response.amountValue()).isEqualByComparingTo(BigDecimal.valueOf(200));
        // 5 earned - (1 previously redeemed + 2 now redeemed) = 2 remaining
        assertThat(response.remainingPoints()).isEqualTo(2L);
        assertThat(account.getTotalPointsRedeemed()).isEqualTo(3L);

        ArgumentCaptor<Redemption> redemptionCaptor = ArgumentCaptor.forClass(Redemption.class);
        verify(redemptionRepository).save(redemptionCaptor.capture());
        assertThat(redemptionCaptor.getValue().getPointsRedeemed()).isEqualTo(2L);
    }

    @Test
    void handle_shouldThrowInsufficientPointsException_whenRequestedPointsExceedAvailableBalance() {
        // Arrange
        String customerId = "cust-2";
        BigDecimal totalAmountPurchased = BigDecimal.valueOf(2000);
        CustomerPointsAccount account = new CustomerPointsAccount(customerId);
        account.setTotalAmountPurchased(totalAmountPurchased);

        RedeemPointsCommand command = new RedeemPointsCommand(customerId, 5L);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));
        when(pointsCalculator.totalPointsEarned(totalAmountPurchased)).thenReturn(2L);

        // Act & Assert
        InsufficientPointsException exception = assertThrows(InsufficientPointsException.class,
                () -> handler.handle(command));

        assertThat(exception.getMessage())
                .contains(customerId)
                .contains("5")
                .contains("2");
        verify(accountRepository, never()).save(any());
        verify(redemptionRepository, never()).save(any());
    }

    @Test
    void handle_shouldThrowInsufficientPointsException_whenCustomerHasNoAccountYet() {
        // Arrange
        String customerId = "cust-3";
        RedeemPointsCommand command = new RedeemPointsCommand(customerId, 1L);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(pointsCalculator.totalPointsEarned(BigDecimal.ZERO)).thenReturn(0L);

        // Act & Assert
        assertThrows(InsufficientPointsException.class, () -> handler.handle(command));

        verify(accountRepository, never()).save(any());
        verify(redemptionRepository, never()).save(any());
    }
}
