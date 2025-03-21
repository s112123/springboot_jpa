package org.demo.server.infra.security.dto;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String email;
    private String password;
}
