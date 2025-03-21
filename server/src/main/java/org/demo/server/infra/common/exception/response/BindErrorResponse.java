package org.demo.server.infra.common.exception.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BindErrorResponse {

    private int status;
    private String field;
    private String message;
    private LocalDateTime createdAt;

    public BindErrorResponse(int status, String field, String message) {
        this(status, field, message, LocalDateTime.now());
    }

    public BindErrorResponse(int status, String field, String message, LocalDateTime createdAt) {
        this.status = status;
        this.field = field;
        this.message = message;
        this.createdAt = createdAt;
    }
}
