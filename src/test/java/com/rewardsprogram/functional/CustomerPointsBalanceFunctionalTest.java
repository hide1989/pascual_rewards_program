package com.rewardsprogram.functional;

import com.rewardsprogram.application.dto.request.RedeemPointsRequest;
import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
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
class CustomerPointsBalanceFunctionalTest {

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

    @Test
    void saldoReflejaPuntosGanadosMenosRedimidosTrasComprasYRedencion() {
        String customerId = newCustomerId();

        client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, BigDecimal.valueOf(4000)))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 1L))
                .exchange()
                .expectStatus().isCreated();

        CustomerPointsBalanceResponse balance = client.get().uri("/api/customers/{customerId}/points", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerPointsBalanceResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(balance.totalPointsEarned()).isEqualTo(4);
        assertThat(balance.totalPointsRedeemed()).isEqualTo(1);
        assertThat(balance.availablePoints()).isEqualTo(3);
    }
}
