package org.demo.server.module.review.dto.details;

import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.review.dto.response.ReviewResponse;
import org.demo.server.module.review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewDetails {

    private Long reviewId;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private Integer star;
    private Long hits;
    private Long goodCount;
    private MemberDetails memberDetails;
    private List<ReviewImageDetails> reviewImagesDetailsList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Controller 에서 응답할 ReviewResponse 객체 생성 (DTO → DTO)
     *
     * @return ReviewResponse
     */
    public ReviewResponse toResponse() {
        return ReviewResponse.builder()
                .reviewId(this.reviewId)
                .memberId(this.memberDetails.getMemberId())
                .writer(this.memberDetails.getUsername())
                .title(this.title)
                .content(this.content)
                .storeName(this.storeName)
                .storeAddress(this.storeAddress)
                .star(this.star)
                .hits(this.hits)
                .goodCount(this.goodCount)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .profileImageDetails(this.memberDetails.getProfileImageDetails())
                .reviewImagesDetailsList(this.reviewImagesDetailsList)
                .build();
    }
}
