package org.demo.server.module.review.service.base;

import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.springframework.data.domain.Page;

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
     * 리뷰 조회
     *
     * @param reviewId 조회 할 reviewId
     * @return 조회된 리뷰
     */
    ReviewDetails findById(long reviewId);
}
