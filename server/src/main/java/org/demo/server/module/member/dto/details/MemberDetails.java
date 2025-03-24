package org.demo.server.module.member.dto.details;

import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.demo.server.module.member.entity.Role;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberDetails {

    private Long memberId;
    private String email;
    private String password;
    private String username;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProfileImageDetails profileImageDetails;

    /**
     * MemberDetails DTO 를 MemberResponse DTO 를 생성한다 (DTO → DTO)
     *
     * @return MemberResponse DTO 를 반환한다
     */
    public MemberResponse toResponse() {
        return MemberResponse.builder()
                .memberId(this.getMemberId())
                .email(this.getEmail())
                .username(this.getUsername())
                .role(this.getRole())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .profileImage(this.getProfileImageDetails())
                .build();
    }
}
