package org.demo.server.module.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberSaveRequest {

    @NotNull(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 아닙니다")
    private String email;

    @NotNull(message = "비밀번호는 필수입니다")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{10,}$",
            message = "비밀번호는 영어와 숫자를 포함하여 10자리 이상이어야 합니다."
    )
    private String password;
}
