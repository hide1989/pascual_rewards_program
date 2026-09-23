package com.rewardsprogram.application.handler;

import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.query.GetCustomerPointsBalanceQuery;
import com.rewardsprogram.domain.model.CustomerPointsAccount;
import com.rewardsprogram.domain.service.PointsCalculator;
import com.rewardsprogram.repository.CustomerPointsAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCustomerPointsBalanceHandlerTest {

    @Mock
    private CustomerPointsAccountRepository accountRepository;

    @Mock
    private PointsCalculator pointsCalculator;

    private GetCustomerPointsBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCustomerPointsBalanceHandler(accountRepository, pointsCalculator);
    }

    @Test
    void handle_shouldReturnZeroBalance_whenCustomerHasNoAccount() {
        // Arrange
        String customerId = "cust-unknown";
        GetCustomerPointsBalanceQuery query = new GetCustomerPointsBalanceQuery(customerId);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(pointsCalculator.totalPointsEarned(BigDecimal.ZERO)).thenReturn(0L);

        // Act
        CustomerPointsBalanceResponse response = handler.handle(query);

        // Assert
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.totalPointsEarned()).isZero();
        assertThat(response.totalPointsRedeemed()).isZero();
        assertThat(response.availablePoints()).isZero();
    }

    @Test
    void handle_shouldReturnComputedBalance_whenCustomerHasAnAccount() {
        // Arrange
        String customerId = "cust-1";
        BigDecimal totalAmountPurchased = BigDecimal.valueOf(3500);
        CustomerPointsAccount account = new CustomerPointsAccount(customerId);
        account.setTotalAmountPurchased(totalAmountPurchased);
        account.setTotalPointsRedeemed(1L);

        GetCustomerPointsBalanceQuery query = new GetCustomerPointsBalanceQuery(customerId);

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));
        when(pointsCalculator.totalPointsEarned(totalAmountPurchased)).thenReturn(3L);

        // Act
        CustomerPointsBalanceResponse response = handler.handle(query);

        // Assert
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.totalPointsEarned()).isEqualTo(3L);
        assertThat(response.totalPointsRedeemed()).isEqualTo(1L);
        assertThat(response.availablePoints()).isEqualTo(2L);
    }
}
