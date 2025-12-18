package com.ksh.walletservice.common.exception;

/**
 * payload: 에러 발생 시 추가로 전달할 수 있는 컨텍스트 정보
 * (예: 요청 ID, 잘못된 파라미터 값 등)
 */
public record ErrorResponse(
        ErrorCode errorCode,
        String message,
        Object payload
) {
}