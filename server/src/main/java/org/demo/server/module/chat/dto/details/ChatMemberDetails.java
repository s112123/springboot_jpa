package org.demo.server.module.chat.dto.details;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.member.dto.details.ProfileImageDetails;

@Getter
@NoArgsConstructor
@ToString
public class ChatMemberDetails {

    private Long memberId;
    private String username;
    private ProfileImageDetails profileImage;
    private boolean hasUnreadMessages;

    public ChatMemberDetails(Follow follow, boolean hasUnreadMessages) {
        this.memberId = follow.getFollowed().getMemberId();
        this.username = follow.getFollowed().getUsername();
        this.profileImage = follow.getFollowed().getProfileImage().toDetails();
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
