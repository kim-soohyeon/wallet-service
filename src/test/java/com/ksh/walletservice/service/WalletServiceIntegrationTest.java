package com.ksh.walletservice.service;

import com.ksh.walletservice.dto.WithdrawalResponse;
import com.ksh.walletservice.entity.Wallet;
import com.ksh.walletservice.repository.WalletHistoryRepository;
import com.ksh.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class WalletServiceIntegrationTest {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    @Autowired
    WalletService walletService;

    private Long walletId;
    private final String transactionId = "TX-UUID-TEST";
    private final Long amount = 100L;
    private final Long balance = 1000L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);
        Wallet saved = walletRepository.save(new Wallet(null, balance, now, now));
        walletId = saved.getWalletId();
    }

    @Test
    @DisplayName("정상 출금: 잔액이 차감되고, 히스토리가 1건 저장된다")
    void withdraw_success() {
        // When
        WithdrawalResponse res = walletService.withdraw(walletId, amount, transactionId);

        // Then
        assertThat(res).isNotNull();
        assertThat(res.walletId()).isEqualTo(walletId);

        Wallet after = walletRepository.findById(walletId).orElseThrow();
        assertThat(after.getBalance()).isEqualTo(balance - amount);

        assertThat(walletHistoryRepository.count()).isEqualTo(1);
        assertThat(walletHistoryRepository.findByTransactionId(transactionId)).isPresent();
    }
}