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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class WalletServiceIdempotentTest {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    WalletHistoryRepository walletHistoryRepository;

    @Autowired
    WalletService walletService;

    private Long walletId;
    private final long balance = 100_000;       // 10만원
    private final long withdrawAmount = 5_000L;   // 5천원
    private final int requestCount = 10;          // 10개 요청


    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);
        Wallet saved = walletRepository.save(new Wallet(null, balance, now, now));
        walletId = saved.getWalletId();
    }

    @Test
    @DisplayName("멱등성: 동일 transactionId 요청은 1번만 출금된다")
    void concurrent_idempotent_test() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(requestCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        String sameTransactionId = "TXN_UUID_12345";

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    walletService.withdraw(walletId, withdrawAmount, sameTransactionId);
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
        boolean txExists = walletHistoryRepository.findByTransactionId(sameTransactionId).isPresent();

        assertThat(result.getBalance()).isEqualTo(balance - withdrawAmount);

        assertThat(fail.get()).isEqualTo(0);
        assertThat(success.get()).isEqualTo(requestCount);
        assertThat(walletHistoryRepository.count()).isEqualTo(1);
        assertThat(walletHistoryRepository.findByTransactionId(sameTransactionId)).isPresent();

        System.out.println("========================================");
        System.out.println("[TEST_END]");
        System.out.println("- finalBalance=" + result.getBalance());
        System.out.println("- expectedBalance=" + (balance - withdrawAmount));
        System.out.println("- success=" + success.get());
        System.out.println("- fail=" + fail.get());
        System.out.println("- historyCount=" + historyCount);
        System.out.println("- txExists=" + txExists);
        System.out.println("========================================");

    }

}
