package org.demo.server.module.review.service.base;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.repository.ReviewRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewFinder {

    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 조회
     *
     * @param reviewId 조회 할 reviewId
     * @return 조회된 리뷰
     */
    public Review getReviewById(long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 글입니다"));
    }
}
