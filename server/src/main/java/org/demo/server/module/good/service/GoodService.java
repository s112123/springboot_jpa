package org.demo.server.module.good.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.service.publisher.MessagePublisher;
import org.demo.server.module.good.dto.request.GoodRequest;
import org.demo.server.module.good.entity.Good;
import org.demo.server.module.good.repository.GoodRepository;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.dto.response.ReviewResponse;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoodService {

    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;
    private final GoodRepository goodRepository;
    private final MessagePublisher messagePublisher;

    /**
     * 좋아요 등록
     *
     * @param request 요청 데이터
     */
    public void save(GoodRequest request) {
        if (!existsGood(request.getReviewId(), request.getMemberId())) {
            Member member = memberFinder.getMemberById(request.getMemberId());
            Review review = reviewFinder.getReviewById(request.getReviewId());

            // 리뷰의 좋아요 개수 +1
            review.plusGoodCount();

            // 좋아요 등록
            Good good = Good.builder()
                    .member(member)
                    .review(review)
                    .build();
            goodRepository.save(good);

            // 좋아요 알림
            messagePublisher.publishLike(member.getMemberId(), review.getReviewId());
        }
    }

    /**
     * 회원이 좋아요를 누른 리뷰 목록
     *
     * @param memberId 회원의 식별자
     * @return 좋아요를 누른 리뷰 목록
     */
    public Page<ReviewResponse> findByMemberId(Long memberId, int page) {
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "goodId"));
        Page<ReviewResponse> reviews = goodRepository.findByMember_MemberId(memberId, pageable)
                .map(good -> good.getReview().toDetails())
                .map(review -> review.toResponse());
        return reviews;
    }

    /**
     * 좋아요를 눌렀는지 여부
     *
     * @param reviewId 리뷰의 식별자
     * @param memberId 회원의 식별자
     * @return 좋아요를 눌렀으면 true, 그렇지 않으면 false
     */
    public boolean existsGood(Long reviewId, Long memberId) {
        return goodRepository.existsByReview_ReviewIdAndMember_MemberId(reviewId, memberId);
    }

    /**
     * 좋아요 취소
     *
     * @param reviewId 리뷰의 식별자
     * @param memberId 회원의 식별자
     */
    public void delete(Long reviewId, Long memberId) {
        if (existsGood(reviewId, memberId)) {
            // 좋아요 수 감소
            Review review = reviewFinder.getReviewById(reviewId);
            review.minusGoodCount();

            // 좋아요 취소
            goodRepository.deleteByReview_ReviewIdAndMember_MemberId(reviewId, memberId);
        }
    }
}
