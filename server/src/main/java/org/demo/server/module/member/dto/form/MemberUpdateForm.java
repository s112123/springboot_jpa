package org.demo.server.module.member.dto.form;

import lombok.Data;

@Data
public class MemberUpdateForm {

    private String username;
    private String password;
    private String originalFileName;
    private String savedFileName;
}
