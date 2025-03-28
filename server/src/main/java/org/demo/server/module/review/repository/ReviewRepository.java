package org.demo.server.module.review.repository;

import org.demo.server.module.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 리뷰 목록
     *
     * @param pageable 페이지네이션
     * @return 리뷰 목록
     */
    Page<Review> findAll(Pageable pageable);

    /**
     * 리뷰 목록
     *
     * @param searchKeyword 검색어
     * @param pageable 페이지네이션
     * @return 리뷰 목록
     */
    @Query("SELECT r FROM Review r WHERE r.title LIKE %:searchKeyword% OR r.content LIKE %:searchKeyword%")
    Page<Review> findAll(@Param("searchKeyword") String searchKeyword, Pageable pageable);

    /**
     * 특정 회원이 작성한 리뷰 목록
     *
     * @param memberId 회원 식별자
     * @param pageable 페이지네이션
     * @return memberId 에 해당하는 회원의 리뷰 목록
     */
    Page<Review> findByMember_MemberId(Long memberId, Pageable pageable);

    /**
     * 선택된 리뷰 모두 삭제
     * 벌크성 쿼리는 영속성 컨텍스트가 아닌 DB 에서 바로 삭제한다
     * 그래서 자식이 외래키로 부모를 참조하고 있는 경우, 부모 데이터를 삭제하면 예외가 발생한다
     * 예외 발생 → SQLIntegrityConstraintViolationException
     * 해결 방법 1) Java 코드로 자식부터 먼저 삭제한다 → 현재 코드에서 사용함
     * 해결 방법 2) DB 에서 직접 ON DELETE CASCADE 제약 조건을 설정한다
     *
     * @param deletedReviewIds 삭제할 리뷰 식별자 목록
     */
    @Modifying
    @Query(value = "DELETE FROM Review r WHERE r.reviewId IN :deletedReviewIds")
    void deleteByReviewIds(@Param("deletedReviewIds") List<Long> deletedReviewIds);

    /**
     * 선택된 리뷰 이미지 모두 삭제
     * Review (1)-(*) ReviewImage 관계이므로 Review 가 부모이다
     * 그래서 deleteByReviewIds() 보다 먼저 실행되어야 한다
     *
     * @param deletedReviewIds 삭제할 리뷰 식별자 목록
     */
    @Modifying
    @Query(value = "DELETE FROM ReviewImage ri WHERE ri.review.reviewId IN :deletedReviewIds")
    void deleteReviewImagesByReviewIds(@Param("deletedReviewIds") List<Long> deletedReviewIds);
}
