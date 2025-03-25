package org.demo.server.module.member.dto.details;

import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.demo.server.module.member.entity.Member;
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
     * (DTO → DTO) MemberDetails → MemberResponse
     *
     * @return MemberResponse
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

    /**
     * (DTO → Entity) MemberDetails → Member
     *
     * @return Member
     */
    public Member toMember() {
        return Member.builder()
                .memberId(this.getMemberId())
                .email(this.email)
                .password(this.password)
                .username(this.username)
                .role(this.role)
                .profileImage(this.profileImageDetails.toProfileImage())
                .build();
    }
}
