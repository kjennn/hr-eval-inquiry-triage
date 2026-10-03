package com.inquirytriage.domain.inquiry.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러 공통 예외 처리. 응답 바디는 {@code {"message": "..."}} 형태로 통일하고,
 * 프론트(ProcessButton 등)가 이 message를 그대로 사용자에게 보여줍니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String message) {}

    // 존재하지 않는 문의/고객사 등 (서비스·컨트롤러에서 IllegalArgumentException으로 던짐)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .findFirst()
            .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    // 잘못된 JSON, 허용되지 않은 enum 값(평가유형 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse("요청 형식이 올바르지 않습니다. 평가유형 등 값을 확인해주세요."));
    }

    // Claude 응답을 JSON으로 해석하지 못한 경우 등 (InquiryNormalizeService)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleBadAiResponse(IllegalStateException e) {
        log.warn("AI 응답 처리 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse("AI 응답을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."));
    }

    // Claude 호출 자체 실패(API 키 누락, 네트워크, 한도 초과 등) 및 그 외 예기치 못한 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("처리 중 예기치 못한 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("서버 처리 중 오류가 발생했습니다: " + e.getMessage()));
    }
}
