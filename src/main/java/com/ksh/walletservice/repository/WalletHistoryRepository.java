package com.ksh.walletservice.repository;

import com.ksh.walletservice.entity.WalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {
    Optional<WalletHistory> findByTransactionId(String transactionId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO wallet_history (transaction_id, wallet_id, amount, balance, status, created_at)
            VALUES (:transactionId, :walletId, :amount, :balance, :status, NOW())
            """,
            nativeQuery = true
    )
    int insertIgnore(
            @Param("transactionId") String transactionId,
            @Param("walletId") Long walletId,
            @Param("amount") Long amount,
            @Param("balance") Long balance,
            @Param("status") String status
    );

    @Modifying
    @Query(value = """
            UPDATE wallet_history
            SET balance = :balance, status = :status
            WHERE transaction_id = :transactionId
            AND status = 'PENDING'
            """,
            nativeQuery = true
    )
    int updateBalanceByTransactionId(
            @Param("transactionId") String transactionId,
            @Param("balance") Long balance,
            @Param("status") String status
    );
}
