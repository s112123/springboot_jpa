package org.demo.server.module.follow.dto.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FollowRequest {

    // 팔로워의 memberId
    private Long followerId;
    // 팔로우의 닉네임
    private String username;
}
