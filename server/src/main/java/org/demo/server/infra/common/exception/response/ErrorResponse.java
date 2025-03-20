package org.demo.server.infra.common.exception.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime createdAt;

    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }

    public ErrorResponse(int status, String message, LocalDateTime createdAt) {
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }
}
