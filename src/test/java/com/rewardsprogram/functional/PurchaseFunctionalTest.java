package com.rewardsprogram.functional;

import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.dto.response.PurchaseResponse;
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
class PurchaseFunctionalTest {

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

    private PurchaseResponse registerPurchase(String customerId, BigDecimal amount) {
        return client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, amount))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PurchaseResponse.class)
                .returnResult()
                .getResponseBody();
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
    void compraConMontoMultiploExactoOtorgaLosPuntosCorrectos() {
        String customerId = newCustomerId();

        PurchaseResponse response = registerPurchase(customerId, BigDecimal.valueOf(3000));

        assertThat(response.pointsEarned()).isEqualTo(3);
        assertThat(response.totalAvailablePoints()).isEqualTo(3);
    }

    @Test
    void compraMenorA1000NoOtorgaPuntosPeroElRemanenteNoSePierde() {
        String customerId = newCustomerId();

        PurchaseResponse response = registerPurchase(customerId, BigDecimal.valueOf(700));

        assertThat(response.pointsEarned()).isEqualTo(0);
        assertThat(getBalance(customerId).availablePoints()).isEqualTo(0);
    }

    @Test
    void compraConDecimalesQueNoCompletanOtroMultiploOtorgaSoloElPuntoEntero() {
        String customerId = newCustomerId();

        PurchaseResponse response = registerPurchase(customerId, new BigDecimal("1999.99"));

        assertThat(response.pointsEarned()).isEqualTo(1);
    }

    @Test
    void carryOverEntreDosComprasAcumulaElRemanenteHastaCompletarElPunto() {
        String customerId = newCustomerId();

        PurchaseResponse first = registerPurchase(customerId, BigDecimal.valueOf(700));
        assertThat(first.pointsEarned()).isEqualTo(0);

        PurchaseResponse second = registerPurchase(customerId, BigDecimal.valueOf(500));
        assertThat(second.pointsEarned()).isEqualTo(1);
        assertThat(second.totalAvailablePoints()).isEqualTo(1);
    }

    @Test
    void secuenciaDeComprasPequenasAcumulaCorrectamenteCruzandoVariosMultiplos() {
        String customerId = newCustomerId();

        PurchaseResponse p1 = registerPurchase(customerId, BigDecimal.valueOf(400));
        PurchaseResponse p2 = registerPurchase(customerId, BigDecimal.valueOf(400));
        PurchaseResponse p3 = registerPurchase(customerId, BigDecimal.valueOf(400));
        PurchaseResponse p4 = registerPurchase(customerId, BigDecimal.valueOf(900));

        assertThat(p1.pointsEarned()).isEqualTo(0);
        assertThat(p2.pointsEarned()).isEqualTo(0);
        assertThat(p3.pointsEarned()).isEqualTo(1);
        assertThat(p4.pointsEarned()).isEqualTo(1);

        assertThat(getBalance(customerId).availablePoints()).isEqualTo(2);
    }

    @Test
    void consultarSaldoDeClienteSinComprasRetorna200ConCero() {
        String customerId = newCustomerId();

        CustomerPointsBalanceResponse balance = getBalance(customerId);

        assertThat(balance.availablePoints()).isEqualTo(0);
        assertThat(balance.totalPointsEarned()).isEqualTo(0);
    }

    @Test
    void compraConMontoNegativoRetorna400ConMensajeDeCampo() {
        String customerId = newCustomerId();

        ErrorResponse response = client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, BigDecimal.valueOf(-5)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.errors()).anyMatch(fieldError -> fieldError.field().equals("amount"));
    }

    @Test
    void compraConMontoCeroRetorna400() {
        String customerId = newCustomerId();

        client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, BigDecimal.ZERO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void compraConCustomerIdVacioRetorna400() {
        ErrorResponse response = client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest("", BigDecimal.valueOf(1000)))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.errors()).anyMatch(fieldError -> fieldError.field().equals("customerId"));
    }

    @Test
    void compraConJsonMalformadoRetorna400() {
        client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"customerId\": \"cust-1\", \"amount\": ")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
