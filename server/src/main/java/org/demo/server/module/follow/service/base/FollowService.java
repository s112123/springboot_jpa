package org.demo.server.module.follow.service.base;

import org.demo.server.module.member.entity.Member;

public interface FollowService {

    // 팔로우 하기
    void following(Member follower, Member followed);

    // 팔로우 취소

    // 팔로우 조회

    // 팔로워 조회
}
