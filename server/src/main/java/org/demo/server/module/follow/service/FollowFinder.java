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
     * 내가 구독한 사람 목록 반환
     * 내가 구독한 사람을 찾으려면 followerId 에 나의 식별자가 들어가야 한다
     *
     * @param followerId 회원 식별자
     * @return 내가 구독한 사람 목록 반환
     */
    public List<MemberDetails> getFollows(Long followerId) {
        List<Follow> findFollows = followRepository.findByFollower_MemberId(followerId);
        return findFollows.stream()
                .map(follow -> follow.getFollowed())
                .map(followed -> followed.toDetails())
                .collect(Collectors.toList());
    }

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
