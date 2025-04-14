package org.demo.server.module.follow.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.service.publisher.MessagePublisher;
import org.demo.server.module.follow.dto.request.FollowRequest;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.follow.repository.FollowRepository;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberFinder memberFinder;
    private final MessagePublisher messagePublisher;

    /**
     * 구독하기
     *
     * @param request 구독 요청 정보
     */
    public void follow(FollowRequest request) {
        if (!isFollower(request.getFollowerId(), request.getUsername())) {
            Member follower = memberFinder.getMemberById(request.getFollowerId());
            Member followed = memberFinder.getMemberByUsername(request.getUsername());

            // 구독 등록
            Follow follow = Follow.builder()
                    .follower(follower)
                    .followed(followed)
                    .build();
            followRepository.save(follow);

            // 구독 알림
            messagePublisher.publishFollow(follower.getMemberId(), followed.getMemberId());
        }
    }

    /**
     * 구독 여부 확인
     *
     * @param followerId 구독자의 식별자
     * @param followed 구독 대상의 닉네임
     * @return 구독했으면 true, 구독하지 않았으면 false
     */
    public boolean isFollower(Long followerId, String followed) {
        // 구독 대상
        Long followedId = memberFinder.getMemberByUsername(followed).getMemberId();
        return followRepository.existsByFollower_MemberIdAndFollowed_MemberId(followerId, followedId);
    }

    /**
     * 구독 취소
     *
     * @param followerId 구독자의 식별자
     * @param followed 구독 대상의 닉네임
     */
    public void unFollow(Long followerId, String followed) {
        if (isFollower(followerId, followed)) {
            // 구독 대상
            Long followedId = memberFinder.getMemberByUsername(followed).getMemberId();
            // 구독 취소
            followRepository.deleteByFollower_MemberIdAndFollowed_MemberId(followerId, followedId);
        }
    }

    /**
     * 내가 구독한 사람 목록 반환
     * 내가 구독한 사람을 찾으려면 followerId 에 나의 식별자가 들어가야 한다
     *
     * @param followerId 회원 식별자
     * @return 내가 구독한 사람 목록 반환
     */
    public List<MemberDetails> findFollows(Long followerId) {
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
    public List<MemberDetails> findFollowers(Long followedId) {
        List<Follow> findFollows = followRepository.findByFollowed_MemberId(followedId);
        return findFollows.stream()
                .map(follow -> follow.getFollower())
                .map(follower -> follower.toDetails())
                .collect(Collectors.toList());
    }

    /**
     * 내가 구독하지 않았지만 나를 구독한 사람 목록 반환
     *
     * @param followedId 회원 식별자
     * @return 내를 구독한 사람 목록 반환
     */
    public List<MemberDetails> findFollower(Long followedId) {
        List<Follow> findFollows = followRepository.findByFollowedNotFollower(followedId);
        return findFollows.stream()
                .map(follow -> follow.getFollower())
                .map(follower -> follower.toDetails())
                .collect(Collectors.toList());
    }
}
