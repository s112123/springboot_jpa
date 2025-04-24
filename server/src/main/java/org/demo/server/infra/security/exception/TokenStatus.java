package org.demo.server.infra.security.exception;

import lombok.Getter;

@Getter
public enum TokenStatus {

    INVALID(601, "Invalid Token"),
    ACCESS_TOKEN_EXPIRED(602, "Access Token is expired"),
    REFRESH_TOKEN_EXPIRED(603, "Refresh Token is expired"),
    MALFORMED(603, "Token is malformed"),
    BAD_SIGNATURE(603, "Bad signature"),
    BAD_TYPE(603, "Bad type"),
    NOT_FOUND(604, "Token is not found");

    private int status;
    private String message;

    TokenStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
