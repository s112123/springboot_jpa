package org.demo.server.module.follow.service.impl;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.follow.repository.FollowRepository;
import org.demo.server.module.follow.service.base.FollowService;
import org.demo.server.module.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    @Override
    public void following(Member follower, Member followed) {
        Follow follow = Follow.builder()
                .follower(follower)
                .followed(followed)
                .build();
        followRepository.save(follow);
    }
}
