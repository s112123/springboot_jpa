package org.demo.server.module.member.util.confirm.dto;

import lombok.Getter;

@Getter
public class ConfirmCodeRequest {

    private String email;
    private String code;
}
