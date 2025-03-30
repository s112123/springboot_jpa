package org.demo.server.module.follow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.ListResponse;
import org.demo.server.module.follow.dto.request.FollowRequest;
import org.demo.server.module.follow.service.FollowService;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 구독하기
     *
     * @param request 구독 요청 정보
     * @return Void
     */
    @PostMapping
    public ResponseEntity<Void> save(
            @RequestBody FollowRequest request
    ) {
        followService.follow(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 구독 여부 확인
     *
     * @param followerId 구독자의 식별자
     * @param followed 구독 대상의 닉네임
     * @return 구독했으면 true, 구독하지 않았으면 false
     */
    @GetMapping("/{memberId}/{username}")
    public ResponseEntity<Boolean> isFollower(
            @PathVariable("memberId") Long followerId,
            @PathVariable("username") String followed
    ) {
        boolean isFollower = followService.isFollower(followerId, followed);
        return ResponseEntity.ok().body(isFollower);
    }

    /**
     * 내가 구독한 사람 목록 반환
     * 내가 구독한 사람을 찾으려면 followerId 에 나의 식별자가 들어가야 한다
     *
     * @param followerId 회원 식별자
     * @return 내가 구독한 사람 목록 반환
     */
    @GetMapping("/{memberId}/follow")
    public ResponseEntity<ListResponse<MemberResponse>> findFollows(
            @PathVariable("memberId") Long followerId
    ) {
        List<MemberResponse> findFollows = followService.findFollows(followerId).stream()
                .map(follow -> follow.toResponse())
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ListResponse<>(findFollows));
    }

    /**
     * 나를 구독한 사람 목록 반환
     * 나를 구독한 사람을 찾으려면 followedId 에 나의 식별자가 들어가야 한다
     *
     * @param followedId 회원 식별자
     * @return 내를 구독한 사람 목록 반환
     */
    @GetMapping("/{memberId}/follower")
    public ResponseEntity<ListResponse<MemberResponse>> findFollower(
            @PathVariable("memberId") Long followedId
    ) {
        List<MemberResponse> findFollowers = followService.findFollower(followedId).stream()
                .map(follower -> follower.toResponse())
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new ListResponse<>(findFollowers));
    }

    /**
     * 구독 취소
     *
     * @param followerId 구독자의 식별자
     * @param followed 구독 대상의 닉네임
     */
    @DeleteMapping("/{memberId}/{username}")
    public ResponseEntity<Boolean> cancelGood(
            @PathVariable("memberId") Long followerId,
            @PathVariable("username") String followed
    ) {
        followService.unFollow(followerId, followed);
        return ResponseEntity.noContent().build();
    }
}
