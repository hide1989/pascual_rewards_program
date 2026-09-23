package com.rewardsprogram.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationException_shouldReturnBadRequestWithFieldErrors_whenBeanValidationFails() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/purchases");

        FieldError fieldError = new FieldError("registerPurchaseRequest", "amount", "amount debe ser mayor que 0");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleValidationException(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().path()).isEqualTo("/api/purchases");
        assertThat(response.getBody().errors()).hasSize(1);
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("amount");
        assertThat(response.getBody().errors().get(0).message()).isEqualTo("amount debe ser mayor que 0");
    }

    @Test
    void handleConstraintViolationException_shouldReturnBadRequestWithFieldErrors_whenPathVariableValidationFails() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/customers//points");

        Path path = mock(Path.class);
        when(path.toString()).thenReturn("customerId");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("customerId no puede estar vacío");

        Set<ConstraintViolation<?>> violations = Set.of(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Act
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleConstraintViolationException(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errors()).hasSize(1);
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("customerId");
    }

    @Test
    void handleNotReadableException_shouldReturnBadRequest_whenRequestBodyIsMalformed() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/purchases");
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("malformed JSON", httpInputMessage);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotReadableException(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("El cuerpo de la solicitud es inválido o está malformado.");
        assertThat(response.getBody().errors()).isEmpty();
        assertThat(exception.getMessage()).isEqualTo("malformed JSON");
    }

    @Test
    void handleInsufficientPoints_shouldReturnConflictWithExceptionMessage_whenBalanceIsNotEnough() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/redemptions");
        InsufficientPointsException exception = new InsufficientPointsException("cust-1", 5L, 2L);

        // Act
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleInsufficientPoints(exception, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo(exception.getMessage());
        assertThat(response.getBody().message()).contains("cust-1").contains("5").contains("2");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError_whenAnUnexpectedExceptionOccurs() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/purchases");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Ocurrió un error inesperado.");
    }
}
