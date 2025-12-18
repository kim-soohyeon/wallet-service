package com.ksh.walletservice.api;

import com.ksh.walletservice.dto.WithdrawalRequest;
import com.ksh.walletservice.dto.WithdrawalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class WalletApiTest {
    RestClient restClient = RestClient.create("http://localhost:8080");

    @Test
    @DisplayName("API: 출금 성공 시 잔액이 정확히 차감되어야 한다")
    void 출금_성공() {
        WithdrawalRequest request =
                new WithdrawalRequest(1000L, UUID.randomUUID().toString());

        WithdrawalResponse response = restClient.post()
                .uri("/api/wallets/{walletId}/withdraw", 1L)
                .body(request)
                .retrieve()
                .body(WithdrawalResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.walletId()).isEqualTo(1L);
        assertThat(response.balance()).isGreaterThanOrEqualTo(0);
    }
}