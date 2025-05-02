package org.demo.server.infra.security.constant;

public abstract class TokenExpiration {

    // Access Token 만료 시간 (minute)
    public static final int ACCESS_TOKEN_EXPIRATION = 10;

    // Refresh Token 만료 시간 (minute)
    public static final int REFRESH_TOKEN_EXPIRATION = 30;
}
