package com.ksh.walletservice.entity;

import com.ksh.walletservice.common.exception.CoreException;
import com.ksh.walletservice.common.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 16, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 12, 16, 10, 0);

        wallet = new Wallet(
                1L,
                10000L,
                createdAt,
                updatedAt
        );
    }

    @Test
    @DisplayName("출금 성공 - 잔액이 정확히 차감되고, UpdatedAt이 갱신된다.")
    void withdraw_success() {
        // Given
        long withdrawalAmount = 3000L;
        long initialBalance = wallet.getBalance();
        // When
        Wallet updatedWallet = wallet.withdraw(withdrawalAmount);
        // Then
        assertEquals(initialBalance - withdrawalAmount, updatedWallet.getBalance());
        assertTrue(updatedWallet.getUpdatedAt().isAfter(wallet.getUpdatedAt()));
    }

    @Test
    @DisplayName("출금 실패 - 잔액보다 큰 금액을 출금하려는 경우 예외 발생")
    void withdraw_failure_insufficient() {
        // Given
        long withdrawalAmount = 15000L; // 현재 잔액(10000L)보다 큰 금액
        long initialBalance = wallet.getBalance();

        // When & Then
        CoreException e = assertThrows(CoreException.class, () -> {
            wallet.withdraw(withdrawalAmount);
        });

        // 예외 메시지 확인
        assertEquals(ErrorType.INSUFFICIENT_BALANCE, e.getErrorType());
        assertEquals("잔액이 부족합니다.", e.getMessage());
        // 잔액이 변하지 않았는지 확인 (불변 객체이므로 당연히 변하지 않아야 함)
        assertEquals(initialBalance, wallet.getBalance(), "출금 실패 시 잔액은 변하지 않아야 합니다.");
    }

    @Test
    @DisplayName("출금 실패 - 출금 금액이 0원 이하일 때")
    void withdraw_failure_invalidAmount() {
        // Given
        long initialBalance = wallet.getBalance();
        long invalidAmount = 0L;

        // When & Then
        CoreException e = assertThrows(CoreException.class, () -> {
            wallet.withdraw(invalidAmount);
        });

        // 예외 메시지 확인
        assertEquals(ErrorType.INVALID_REQUEST, e.getErrorType());
        assertEquals("요청 값이 올바르지 않습니다.", e.getMessage());
        // 잔액이 변하지 않았는지 확인
        assertEquals(initialBalance, wallet.getBalance(), "출금 실패 시 잔액은 변하지 않아야 합니다.");
    }
}