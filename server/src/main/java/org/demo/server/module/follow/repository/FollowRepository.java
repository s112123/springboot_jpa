package org.demo.server.module.follow.repository;

import org.demo.server.module.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * 구독 여부
     *
     * @param followerId 구독 회원의 식별자
     * @param followedId 구독 대상의 식별자
     * @return
     */
    boolean existsByFollower_MemberIdAndFollowed_MemberId(Long followerId, Long followedId);

    /**
     * 구독 취소
     *
     * @param followerId 구독 회원의 식별자
     * @param followedId 구독 대상의 식별자
     */
    void deleteByFollower_MemberIdAndFollowed_MemberId(Long followerId, Long followedId);

    /**
     * 내가 구독한 사람 목록 반환
     * 내가 구독한 사람을 찾으려면 followerId 에 나의 식별자가 들어가야 한다
     *
     * @param followerId 회원 식별자
     * @return 내가 구독한 사람 목록 반환
     */
    List<Follow> findByFollower_MemberId(Long followerId);

    /**
     * 나를 구독한 사람 목록 반환
     * 나를 구독한 사람을 찾으려면 followedId 에 나의 식별자가 들어가야 한다
     *
     * @param followedId 회원 식별자
     * @return 내를 구독한 사람 목록 반환
     */
    List<Follow> findByFollowed_MemberId(Long followedId);

    /**
     * 내가 구독하지 않았지만 나를 구독한 사람 목록 반환
     *
     * @param followedId 회원 식별자
     * @return 내를 구독한 사람 목록 반환
     */
    @Query("SELECT f1 FROM Follow f1 " +
           "WHERE f1.followed.memberId = :memberId " +
           "  AND NOT EXISTS (SELECT 1 FROM Follow f2 " +
           "                  WHERE f1.follower.memberId = f2.followed.memberId)")
    List<Follow> findByFollowedNotFollower(@Param("memberId") Long followedId);
}
