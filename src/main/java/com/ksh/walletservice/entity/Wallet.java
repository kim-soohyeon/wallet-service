package com.ksh.walletservice.entity;

import com.ksh.walletservice.common.exception.CoreException;
import com.ksh.walletservice.common.exception.ErrorType;
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
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    @Comment("월렛 ID")
    private Long walletId;

    @Column(nullable = false)
    @Comment("잔액")
    private Long balance;

    @Column(name = "created_at", nullable = false)
    @Comment("생성 일시")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Comment("수정 일시")
    private LocalDateTime updatedAt;

    public Wallet withdraw(Long amount) {

        if (amount <= 0) {
            throw new CoreException(ErrorType.INVALID_REQUEST, amount);
        }

        if (balance < amount) {
            throw new CoreException(ErrorType.INSUFFICIENT_BALANCE, amount);
        }

        return new Wallet(walletId, balance - amount, createdAt, LocalDateTime.now());
    }
}
