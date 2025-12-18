package com.ksh.walletservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    TX_NOT_FOUND(
            ErrorCode.NOT_FOUND,
            "트랜잭션을 찾을 수 없습니다."
    ),

    WALLET_NOT_FOUND(
            ErrorCode.NOT_FOUND,
            "월렛을 찾을 수 없습니다."
    ),

    INVALID_REQUEST(
            ErrorCode.BAD_REQUEST,
            "요청 값이 올바르지 않습니다."
    ),

    INSUFFICIENT_BALANCE(
            ErrorCode.BAD_REQUEST,
            "잔액이 부족합니다."
    );

    private final ErrorCode errorCode;
    private final String message;
}