package org.demo.server.module.chat.dto.details;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.member.dto.details.ProfileImageDetails;

@Slf4j
@Getter
@NoArgsConstructor
@ToString
public class ChatMemberDetails {

    private Long memberId;
    private String username;
    private ProfileImageDetails profileImage;

    public ChatMemberDetails(Follow follow) {
        this.memberId = follow.getFollowed().getMemberId();
        this.username = follow.getFollowed().getUsername();
        this.profileImage = follow.getFollowed().getProfileImage().toDetails();
    }
}
