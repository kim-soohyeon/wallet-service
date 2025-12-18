package com.ksh.walletservice.controller;

import com.ksh.walletservice.dto.WithdrawalRequest;
import com.ksh.walletservice.dto.WithdrawalResponse;
import com.ksh.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WalletController {

    private final WalletService walletService;
    @PostMapping("/api/wallets/{walletId}/withdraw")
    public ResponseEntity<WithdrawalResponse> withdraw(
            @PathVariable Long walletId,
            @RequestBody WithdrawalRequest request
    ) {
        return ResponseEntity.ok(walletService.withdraw(walletId, request.amount(), request.transactionId()));
    }
}
