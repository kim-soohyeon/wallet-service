package com.ksh.walletservice.service;

import com.ksh.walletservice.common.exception.CoreException;
import com.ksh.walletservice.common.exception.ErrorType;
import com.ksh.walletservice.dto.WithdrawalResponse;
import com.ksh.walletservice.dto.WithdrawalStatus;
import com.ksh.walletservice.entity.Wallet;
import com.ksh.walletservice.entity.WalletHistory;
import com.ksh.walletservice.repository.WalletHistoryRepository;
import com.ksh.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletHistoryRepository walletHistoryRepository;

    @Transactional
    public WithdrawalResponse withdraw(Long walletId, Long amount, String transactionId) {

        Wallet wallet = walletRepository.findLockedByWalletId(walletId)
                .orElseThrow(() -> new CoreException(ErrorType.WALLET_NOT_FOUND, walletId));

        int inserted = walletHistoryRepository.insertIgnore(
                transactionId, walletId, amount, wallet.getBalance(), WithdrawalStatus.PENDING.name()
        );
        if (inserted == 0) {
            WalletHistory existing = walletHistoryRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new CoreException(ErrorType.TX_NOT_FOUND, transactionId));
            return WithdrawalResponse.from(existing);
        }

        Wallet updatedWallet = wallet.withdraw(amount);
        walletRepository.save(updatedWallet);

        walletHistoryRepository.updateBalanceByTransactionId(
                transactionId, updatedWallet.getBalance(), WithdrawalStatus.SUCCESS.name()
        );

        WalletHistory finalHistory = walletHistoryRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CoreException(ErrorType.TX_NOT_FOUND, transactionId));

        return WithdrawalResponse.from(finalHistory);
    }
}