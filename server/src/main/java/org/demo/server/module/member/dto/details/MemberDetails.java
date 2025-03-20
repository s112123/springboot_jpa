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
     * MemberDetails DTO 를 MemberResponse DTO 를 생성한다
     *
     * @param memberDetails MemberResponse DTO 로 변경할 MemberDetails DTO
     * @return MemberResponse DTO 를 반환한다
     */
    public static MemberResponse toMemberResponse(MemberDetails memberDetails) {
        return MemberResponse.builder()
                .memberId(memberDetails.getMemberId())
                .username(memberDetails.getUsername())
                .role(memberDetails.getRole())
                .createdAt(memberDetails.getCreatedAt())
                .updatedAt(memberDetails.getUpdatedAt())
                .profileImage(memberDetails.getProfileImageDetails())
                .build();
    }
}
