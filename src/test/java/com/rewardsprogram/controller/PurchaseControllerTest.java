package com.rewardsprogram.controller;

import com.rewardsprogram.application.command.RegisterPurchaseCommand;
import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.PurchaseResponse;
import com.rewardsprogram.application.handler.RegisterPurchaseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private RegisterPurchaseHandler registerPurchaseHandler;

    private PurchaseController controller;

    @BeforeEach
    void setUp() {
        controller = new PurchaseController(registerPurchaseHandler);
    }

    @Test
    void registerPurchase_shouldReturnCreatedWithHandlerResult_whenRequestIsValid() {
        // Arrange
        RegisterPurchaseRequest request = new RegisterPurchaseRequest("cust-1", BigDecimal.valueOf(1500));
        PurchaseResponse expectedResponse =
                new PurchaseResponse("cust-1", 1L, BigDecimal.valueOf(1500), 1L, 1L);

        when(registerPurchaseHandler.handle(any(RegisterPurchaseCommand.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<PurchaseResponse> result = controller.registerPurchase(request);

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedResponse);

        ArgumentCaptor<RegisterPurchaseCommand> commandCaptor = ArgumentCaptor.forClass(RegisterPurchaseCommand.class);
        verify(registerPurchaseHandler).handle(commandCaptor.capture());
        assertThat(commandCaptor.getValue().customerId()).isEqualTo("cust-1");
        assertThat(commandCaptor.getValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }
}
