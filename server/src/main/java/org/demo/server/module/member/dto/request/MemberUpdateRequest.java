package org.demo.server.module.member.dto.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MemberUpdateRequest {

    private String username;
    private String password;
    private String originalFileName;
    private String savedFileName;
}
