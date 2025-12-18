package com.ksh.walletservice.dto;

public enum WithdrawalStatus {
    PENDING,   // 출금 처리 중 (insert 직후)
    SUCCESS,   // 출금 성공
    FAILED     // 출금 실패 (잔액 부족 등)
}
