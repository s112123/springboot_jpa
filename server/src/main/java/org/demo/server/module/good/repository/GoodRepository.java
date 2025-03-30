package org.demo.server.module.good.repository;

import org.demo.server.module.good.dto.response.GoodResponse;
import org.demo.server.module.good.entity.Good;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodRepository extends JpaRepository<Good, Long> {

    /**
     * 좋아요를 눌렀는지 확인
     *
     * @param reviewId 리뷰의 식별자
     * @param memberId 회원의 식별자
     * @return good 테이블에 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByReview_ReviewIdAndMember_MemberId(Long reviewId, Long memberId);

    /**
     * 회원이 좋아요를 누른 리뷰 목록
     *
     * @param memberId 회원의 식별자
     * @return
     */
    Page<Good> findByMember_MemberId(Long memberId, Pageable pageable);

    /**
     * 좋아요 취소
     *
     * @param reviewId 리뷰의 식별자
     * @param memberId 회원의 식별자
     */
    void deleteByReview_ReviewIdAndMember_MemberId(Long reviewId, Long memberId);
}
