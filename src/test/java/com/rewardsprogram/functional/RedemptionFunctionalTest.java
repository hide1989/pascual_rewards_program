package com.rewardsprogram.functional;

import com.rewardsprogram.application.dto.request.RedeemPointsRequest;
import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
import com.rewardsprogram.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RedemptionFunctionalTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private String newCustomerId() {
        return "cust-" + UUID.randomUUID();
    }

    private void purchase(String customerId, BigDecimal amount) {
        client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, amount))
                .exchange()
                .expectStatus().isCreated();
    }

    private CustomerPointsBalanceResponse getBalance(String customerId) {
        return client.get().uri("/api/customers/{customerId}/points", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerPointsBalanceResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    void redencionParcialExitosaDevuelveValorEnPesosYSaldoRestante() {
        String customerId = newCustomerId();
        purchase(customerId, BigDecimal.valueOf(5000));

        RedemptionResponse response = client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 2L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RedemptionResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.amountValue()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.remainingPoints()).isEqualTo(3);
        assertThat(getBalance(customerId).availablePoints()).isEqualTo(3);
    }

    @Test
    void redencionDelTotalDisponibleDejaElSaldoEnCero() {
        String customerId = newCustomerId();
        purchase(customerId, BigDecimal.valueOf(2000));

        RedemptionResponse response = client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 2L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RedemptionResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.remainingPoints()).isEqualTo(0);
    }

    @Test
    void redencionConSaldoInsuficienteRetorna409ConMensajeClaro() {
        String customerId = newCustomerId();
        purchase(customerId, BigDecimal.valueOf(1000));

        ErrorResponse response = client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 5L))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.message()).contains("5").contains("1");
    }

    @Test
    void redencionDeClienteSinComprasPreviasRetorna409() {
        String customerId = newCustomerId();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 1L))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void dobleRedencionQueEnConjuntoSuperaElSaldoFallaEnLaSegunda() {
        String customerId = newCustomerId();
        purchase(customerId, BigDecimal.valueOf(3000));

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 2L))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 2L))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void redencionConPuntosCeroRetorna400() {
        String customerId = newCustomerId();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 0L))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void redencionConPuntosNegativosRetorna400() {
        String customerId = newCustomerId();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, -3L))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void redencionConCustomerIdVacioRetorna400() {
        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest("", 1L))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void redencionConPuntosNoEnterosRetorna400() {
        String customerId = newCustomerId();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"customerId\": \"" + customerId + "\", \"points\": 2.5 }")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
