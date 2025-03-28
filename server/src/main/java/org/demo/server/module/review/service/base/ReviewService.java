package org.demo.server.module.review.service.base;

import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.dto.form.ReviewUpdateForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    /**
     * 리뷰 등록
     *
     * @param form 입력된 리뷰 정보
     * @return 저장된 리뷰 정보
     */
    ReviewDetails save(ReviewSaveForm form);

    /**
     * 리뷰 목록
     *
     * @param page 현재 페이지
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 createdAt 으로 내림차순
     */
    Page<ReviewDetails> findAll(int page);

    /**
     * 리뷰 목록
     * sort 옵션은 전체 (0, "createdAt"), 최신 순 (1, "createdAt"), 평점 순 (2, "star")
     *
     * @param page 현재 페이지
     * @param sort 정렬 기준
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 sort 로 내림차순
     */
    Page<ReviewDetails> findAll(int page, int sort);

    /**
     * 리뷰 목록
     * sort 옵션은 전체 (0, "createdAt"), 최신 순 (1, "createdAt"), 평점 순 (2, "star")
     *
     * @param page 현재 페이지
     * @param sort 정렬 기준
     * @param searchKeyword 검색어
     * @return 검색된 결과에서 현재 페이지에 해당하는 10개의 리뷰 목록을 sort 로 내림차순
     */
    Page<ReviewDetails> findAll(int page, int sort, String searchKeyword);

    /**
     * 특정 회원이 작성한 리뷰 목록
     *
     * @param memberId 회원 식별자
     * @param page 조회할 페이지 번호
     * @return 리뷰 목록
     */
    Page<ReviewDetails> findByMemberId(Long memberId, int page);

    /**
     * 리뷰 조회
     *
     * @param reviewId 조회 할 reviewId
     * @return 조회된 리뷰
     */
    ReviewDetails findById(long reviewId);

    /**
     * 리뷰 수정
     *
     * @param form 수정할 리뷰 정보
     * @return 수정된 리뷰 정보
     */
    ReviewDetails update(ReviewUpdateForm form);

    /**
     * 리뷰 삭제
     * 리뷰 글에 있는 이미지까지 모두 삭제해야 한다
     * 리뷰 이미지 위치 → uploads > reviews > memberId > reviewId
     * 
     * @param reviewId 삭제할 리뷰 식별자
     */
    void delete(Long reviewId);

    /**
     * 선택된 리뷰 모두 삭제
     *
     * @param deletedReviewIds 삭제할 리뷰 식별자 목록
     */
    void deleteSelectedReviews(List<Long> deletedReviewIds);
}
