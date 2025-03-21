package org.demo.server.module.member.util.confirm.dto;

import lombok.Getter;

@Getter
public class ConfirmCodeResponse {

    // success, fail
    private String status;
    private String message;
}
