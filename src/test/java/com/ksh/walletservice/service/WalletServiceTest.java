package com.ksh.walletservice.service;

import com.ksh.walletservice.common.exception.CoreException;
import com.ksh.walletservice.common.exception.ErrorType;
import com.ksh.walletservice.dto.WithdrawalResponse;
import com.ksh.walletservice.dto.WithdrawalStatus;
import com.ksh.walletservice.entity.Wallet;
import com.ksh.walletservice.entity.WalletHistory;
import com.ksh.walletservice.repository.WalletHistoryRepository;
import com.ksh.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    WalletRepository walletRepository;

    @Mock
    WalletHistoryRepository walletHistoryRepository;

    @InjectMocks
    WalletService walletService;

    private final Long walletId = 1L;
    private final String transactionId = "TX-UUID-TEST";
    private final Long amount = 100L;
    private final Long balance = 1000L;

    @Test
    void withdraw_success() {
        Long finalBalance = balance - amount;
        // Given
        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);
        Wallet wallet = new Wallet(walletId, balance, now, now);
        WalletHistory successHistory = new WalletHistory(
                1L, transactionId, walletId, amount, finalBalance, WithdrawalStatus.SUCCESS, now
        );
        given(walletHistoryRepository.insertIgnore(any(), any(), any(), any(), any())).willReturn(1);
        given(walletRepository.findLockedByWalletId(walletId)).willReturn(Optional.of(wallet));
        given(walletHistoryRepository.findByTransactionId(transactionId)).willReturn(Optional.of(successHistory));

        // When
        WithdrawalResponse response = walletService.withdraw(walletId, amount, transactionId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualTo(finalBalance);

        verify(walletRepository).save(any(Wallet.class)); // 지갑 잔액 저장 확인
        verify(walletHistoryRepository).updateBalanceByTransactionId(eq(transactionId), eq(finalBalance), eq(WithdrawalStatus.SUCCESS.name()));
        verify(walletHistoryRepository).insertIgnore(
                eq(transactionId),
                eq(walletId),
                eq(amount),
                any(),
                eq(WithdrawalStatus.PENDING.name())
        );}

    @Test
    @DisplayName("동일 transactionId 재요청 시 실패, 기존 결과를 반환하고 출금을 막아야 한다.")
    void withdraw_failure_idempotency() {
        // Given
        Long finalBalance = balance - amount;

        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);

        WalletHistory history = new WalletHistory(
                1L,
                transactionId,
                walletId,
                amount,
                finalBalance,
                WithdrawalStatus.SUCCESS,
                now
        );

        given(walletHistoryRepository.insertIgnore(any(), any(), any(), any(), any())).willReturn(0);
        given(walletHistoryRepository.findByTransactionId(transactionId)).willReturn(Optional.of(history));

        // When
        WithdrawalResponse response = walletService.withdraw(walletId, amount, transactionId);

        // Then

        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualTo(finalBalance); // 900L

        verify(walletRepository, never()).findLockedByWalletId(any());
        verify(walletRepository, never()).save(any());
        verify(walletHistoryRepository, never()).updateBalanceByTransactionId(any(), any(), any());
    }


    @Test
    @DisplayName("잔액 부족 시 실패 CoreException을 던져야 한다.")
    void withdraw_failure_insufficientBalance() {
        // Given
        Long largeAmount = 5000L;

        LocalDateTime now = LocalDateTime.of(2025, 12, 16, 10, 0);
        WalletHistory history = new WalletHistory(
                1L,
                transactionId,
                walletId,
                largeAmount,
                balance,
                WithdrawalStatus.PENDING,
                now
        );
        Wallet wallet = new Wallet(walletId, balance, now, now);

        given(walletRepository.findLockedByWalletId(walletId)).willReturn(Optional.of(wallet));
        given(walletHistoryRepository.insertIgnore(any(), any(), any(), any(), any()))
                .willReturn(1);

        // When & Then
        CoreException e = assertThrows(CoreException.class, () -> {
            walletService.withdraw(walletId, largeAmount, transactionId);
        });

        assertThat(e.getErrorType()).isEqualTo(ErrorType.INSUFFICIENT_BALANCE);
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletHistoryRepository, never()).updateBalanceByTransactionId(any(), any(), any());
    }

}