package org.demo.server.module.follow.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.follow.repository.FollowRepository;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FollowFinder {

    private final FollowRepository followRepository;

    /**
     * 나를 구독한 사람 목록 반환
     *
     * @param followedId 회원 식별자
     * @return 나를 구독한 사람 목록 반환
     */
    public List<MemberDetails> getFollowers(Long followedId) {
        List<Follow> findFollows = followRepository.findByFollowed_MemberId(followedId);
        return findFollows.stream()
                .map(follow -> follow.getFollower())
                .map(follower -> follower.toDetails())
                .collect(Collectors.toList());
    }
}
