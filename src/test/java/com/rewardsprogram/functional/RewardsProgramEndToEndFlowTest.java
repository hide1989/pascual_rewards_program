package com.rewardsprogram.functional;

import com.rewardsprogram.application.dto.request.RedeemPointsRequest;
import com.rewardsprogram.application.dto.request.RegisterPurchaseRequest;
import com.rewardsprogram.application.dto.response.CustomerPointsBalanceResponse;
import com.rewardsprogram.application.dto.response.RedemptionResponse;
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
class RewardsProgramEndToEndFlowTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
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
    void flujoCompletoDeComprasConsultaYRedencionDePuntos() {
        String customerId = "cust-" + UUID.randomUUID();

        client.post().uri("/api/purchases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegisterPurchaseRequest(customerId, BigDecimal.valueOf(3500)))
                .exchange()
                .expectStatus().isCreated();

        assertThat(getBalance(customerId).availablePoints()).isEqualTo(3);

        RedemptionResponse partialRedemption = client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 2L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RedemptionResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(partialRedemption.remainingPoints()).isEqualTo(1);

        assertThat(getBalance(customerId).availablePoints()).isEqualTo(1);

        RedemptionResponse finalRedemption = client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 1L))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RedemptionResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(finalRedemption.remainingPoints()).isEqualTo(0);

        assertThat(getBalance(customerId).availablePoints()).isEqualTo(0);

        client.post().uri("/api/redemptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RedeemPointsRequest(customerId, 1L))
                .exchange()
                .expectStatus().isEqualTo(409);
    }
}
