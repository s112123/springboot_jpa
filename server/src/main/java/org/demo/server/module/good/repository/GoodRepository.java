package org.demo.server.module.good.repository;

import org.demo.server.module.good.entity.Good;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    /**
     * 마이 페이지에서 리뷰를 선택하여 벌크 삭제할 때, good 테이블의 review_id 의 외래키를 끊어야 한다
     * 벌크 삭제는 cascade 옵션이 동작되지 않으므로 직접 외래키 테이블을 먼저 삭제해야 한다
     *
     * @param reviewIds
     */
    @Modifying
    @Query("DELETE FROM Good g WHERE g.review.reviewId IN :reviewIds")
    void deleteByReviewIdIn(@Param("reviewIds") List<Long> reviewIds);
}
