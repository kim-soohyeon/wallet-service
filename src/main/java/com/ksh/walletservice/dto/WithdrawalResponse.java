package com.ksh.walletservice.dto;

import com.ksh.walletservice.entity.WalletHistory;

public record WithdrawalResponse(
        Long walletId,
        Long balance,
        String status
){
    public static WithdrawalResponse from(WalletHistory history) {
        return new WithdrawalResponse(
                history.getWalletId(),
                history.getBalance(),
                history.getStatus().name()
        );
    }
}
