package org.demo.server.module.good.dto.response;

import lombok.Getter;
import org.demo.server.module.good.entity.Good;

@Getter
public class GoodResponse {

    private Long memberId;
    private Long reviewId;

    public GoodResponse(Good good) {
        this.memberId = good.getMember().getMemberId();
        this.reviewId = good.getReview().getReviewId();
    }
}
