package com.ksh.walletservice.service;

import com.ksh.walletservice.entity.Wallet;
import com.ksh.walletservice.repository.WalletHistoryRepository;
import com.ksh.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class WalletServiceConcurrencyTest {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    @Autowired
    WalletService walletService;

    private Long walletId;
    private final long balance = 1_000_000L;       // 100만원
    private final long withdrawAmount = 10_000L;   // 1만원
    private final int requestCount = 100;          // 100개 요청

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);
        Wallet saved = walletRepository.save(new Wallet(null, balance, now, now));
        walletId = saved.getWalletId();
    }

    @Test
    @DisplayName("동시성: walletId=1에 transactionId가 모두 다르면 모두 출금되어 0원이 된다.")
    void concurrent_withdraw_test() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(requestCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    walletService.withdraw(walletId, withdrawAmount, String.valueOf(UUID.randomUUID()));
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        Wallet result = walletRepository.findById(walletId).orElseThrow();
        long historyCount = walletHistoryRepository.count();

        assertThat(success.get()).isEqualTo(requestCount);
        assertThat(fail.get()).isEqualTo(0);
        assertThat(result.getBalance()).isEqualTo(0L);
        assertThat(walletHistoryRepository.count()).isEqualTo(requestCount);

        System.out.println("========================================");
        System.out.println("[TEST_END]");
        System.out.println("- finalBalance=" + result.getBalance());
        System.out.println("- expectedBalance=" + (balance - requestCount * withdrawAmount));
        System.out.println("- success=" + success.get());
        System.out.println("- fail=" + fail.get());
        System.out.println("- historyCount=" + historyCount);
        System.out.println("========================================");

    }

}