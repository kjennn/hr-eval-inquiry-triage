package com.inquirytriage.domain.inquiry.service;

/**
 * Claude 응답이 비어 있거나 기대한 형식(JSON)이 아닐 때 던지는 예외.
 * Hibernate/Spring 등이 던지는 일반 {@link IllegalStateException}과 구분하기 위해 전용 타입을 씁니다.
 * 메시지에는 모델 응답 본문(문의 원문이 섞여 있을 수 있음)을 넣지 않습니다.
 */
public class AiResponseException extends RuntimeException {

    public AiResponseException(String message) {
        super(message);
    }

    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
