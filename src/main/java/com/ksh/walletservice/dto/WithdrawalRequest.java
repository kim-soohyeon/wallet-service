package com.ksh.walletservice.dto;

public record WithdrawalRequest(Long amount, String transactionId) {
}
