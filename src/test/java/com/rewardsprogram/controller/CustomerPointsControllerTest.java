package com.rewardsprogram.controller;

import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.handler.GetCustomerPointsBalanceHandler;
import com.rewardsprogram.application.query.GetCustomerPointsBalanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerPointsControllerTest {

    @Mock
    private GetCustomerPointsBalanceHandler getCustomerPointsBalanceHandler;

    private CustomerPointsController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerPointsController(getCustomerPointsBalanceHandler);
    }

    @Test
    void getPointsBalance_shouldReturnHandlerResult_whenCustomerIdIsValid() {
        // Arrange
        CustomerPointsBalanceResponse expectedResponse =
                new CustomerPointsBalanceResponse("cust-1", 5L, 2L, 3L);

        when(getCustomerPointsBalanceHandler.handle(any(GetCustomerPointsBalanceQuery.class)))
                .thenReturn(expectedResponse);

        // Act
        CustomerPointsBalanceResponse response = controller.getPointsBalance("cust-1");

        // Assert
        assertThat(response).isEqualTo(expectedResponse);

        ArgumentCaptor<GetCustomerPointsBalanceQuery> queryCaptor =
                ArgumentCaptor.forClass(GetCustomerPointsBalanceQuery.class);
        verify(getCustomerPointsBalanceHandler).handle(queryCaptor.capture());
        assertThat(queryCaptor.getValue().customerId()).isEqualTo("cust-1");
    }
}
