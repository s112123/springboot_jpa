package org.demo.server.module.member.dto.response;

import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.dto.details.ProfileImageDetails;
import org.demo.server.module.member.entity.Role;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {

    private Long memberId;
    private String username;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProfileImageDetails profileImage;
}
