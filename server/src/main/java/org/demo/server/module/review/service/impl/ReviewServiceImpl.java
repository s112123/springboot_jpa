package org.demo.server.module.review.service.impl;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.repository.ReviewRepository;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.demo.server.module.review.service.base.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;

    /**
     * 리뷰 등록
     *
     * @param form 입력된 리뷰 정보
     * @return 저장된 리뷰 정보
     */
    @Override
    public ReviewDetails save(ReviewSaveForm form) {
        // 회원 엔티티 조회
        Member findMember = memberFinder.getMemberByUsername(form.getWriter());

        // 리뷰 엔티티 생성
        Review review = Review.builder()
                .title(form.getTitle())
                .content(form.getContent())
                .storeName(form.getStoreName())
                .storeAddress(form.getStoreAddress())
                .star(form.getStar())
                .hits(0L)
                .member(findMember)
                .build();

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);
        return savedReview.toDetails();
    }

    /**
     * 리뷰 목록
     *
     * @param page 현재 페이지
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 createdAt 으로 내림차순
     */
    @Override
    public Page<ReviewDetails> findAll(int page) {
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> findReviews = reviewRepository.findAll(pageable);
        Page<ReviewDetails> findReviewsDetails = findReviews.map(review -> review.toDetails());
        return findReviewsDetails;
    }

    /**
     * 리뷰 조회
     *
     * @param reviewId 조회 할 reviewId
     * @return 조회된 리뷰
     */
    @Override
    public ReviewDetails findById(long reviewId) {
        Review findReview = reviewFinder.getReviewById(reviewId);
        return findReview.toDetails();
    }
}
