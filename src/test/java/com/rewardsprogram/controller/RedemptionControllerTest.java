package com.rewardsprogram.controller;

import com.rewardsprogram.application.command.RedeemPointsCommand;
import com.rewardsprogram.application.dto.request.RedeemPointsRequest;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
import com.rewardsprogram.application.handler.RedeemPointsHandler;
import com.rewardsprogram.exception.InsufficientPointsException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedemptionControllerTest {

    @Mock
    private RedeemPointsHandler redeemPointsHandler;

    private RedemptionController controller;

    @BeforeEach
    void setUp() {
        controller = new RedemptionController(redeemPointsHandler);
    }

    @Test
    void redeemPoints_shouldReturnCreatedWithHandlerResult_whenRequestIsValid() {
        // Arrange
        RedeemPointsRequest request = new RedeemPointsRequest("cust-1", 2L);
        RedemptionResponse expectedResponse =
                new RedemptionResponse("cust-1", 5L, 2L, BigDecimal.valueOf(200), 1L);

        when(redeemPointsHandler.handle(any(RedeemPointsCommand.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<RedemptionResponse> result = controller.redeemPoints(request);

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedResponse);

        ArgumentCaptor<RedeemPointsCommand> commandCaptor = ArgumentCaptor.forClass(RedeemPointsCommand.class);
        verify(redeemPointsHandler).handle(commandCaptor.capture());
        assertThat(commandCaptor.getValue().customerId()).isEqualTo("cust-1");
        assertThat(commandCaptor.getValue().points()).isEqualTo(2L);
    }

    @Test
    void redeemPoints_shouldPropagateInsufficientPointsException_whenHandlerRejectsTheRedemption() {
        // Arrange
        RedeemPointsRequest request = new RedeemPointsRequest("cust-1", 100L);

        when(redeemPointsHandler.handle(any(RedeemPointsCommand.class)))
                .thenThrow(new InsufficientPointsException("cust-1", 100L, 0L));

        // Act & Assert
        assertThrows(InsufficientPointsException.class, () -> controller.redeemPoints(request));
    }
}
