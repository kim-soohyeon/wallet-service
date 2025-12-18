package com.ksh.walletservice.entity;


import com.ksh.walletservice.dto.WithdrawalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "wallet_history")
public class WalletHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_history_id")
    @Comment("월렛 내역 아이디")
    private Long walletHistoryId;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 36)
    @Comment("거래 고유 아이디")
    private String transactionId;

    @Column(name = "wallet_id")
    @Comment("월렛 아이디")
    private Long walletId;

    @Column(nullable = false)
    @Comment("출금액")
    private Long amount;

    @Column(nullable = false)
    @Comment("잔액")
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("출금 상태")
    private WithdrawalStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
