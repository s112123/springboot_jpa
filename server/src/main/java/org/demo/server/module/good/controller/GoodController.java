package org.demo.server.module.good.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.module.good.dto.request.GoodRequest;
import org.demo.server.module.good.service.GoodService;
import org.demo.server.module.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/goods")
@RequiredArgsConstructor
public class GoodController {

    private final GoodService goodService;

    /**
     * 좋아요 등록
     *
     * @param request 요청 데이터
     * @return Void
     */
    @PostMapping
    public ResponseEntity<Void> save(
            @RequestBody GoodRequest request
    ) {
        goodService.save(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원이 좋아요를 누른 리뷰 목록
     *
     * @param memberId 회원의 식별자
     * @return
     */
    @GetMapping("/{memberId}/pages/{page}")
    public ResponseEntity<PagedListResponse> findGoodReviews(
            @PathVariable("memberId") Long memberId,
            @PathVariable("page") int page
    ) {
        Page<ReviewResponse> reviews = goodService.findByMemberId(memberId, page);
        return ResponseEntity.ok().body(new PagedListResponse<>(reviews));
    }

    /**
     * 좋아요를 눌렀는지 여부
     *
     * @param memberId 회원의 식별자
     * @param reviewId 리뷰의 식별자
     * @return 좋아요를 눌렀으면 true, 그렇지 않으면 false
     */
    @GetMapping("/{reviewId}/{memberId}")
    public ResponseEntity<Boolean> isGood(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("memberId") Long memberId
    ) {
        boolean isGood = goodService.existsGood(reviewId, memberId);
        return ResponseEntity.ok().body(isGood);
    }

    /**
     * 좋아요 취소
     *
     * @param reviewId 리뷰의 식별자
     * @param memberId 회원의 식별자
     * @return Void
     */
    @DeleteMapping("/{reviewId}/{memberId}")
    public ResponseEntity<Boolean> cancelGood(
            @PathVariable("reviewId") Long reviewId,
            @PathVariable("memberId") Long memberId
    ) {
        goodService.delete(reviewId, memberId);
        return ResponseEntity.noContent().build();
    }
}
