package org.demo.server.module.review.dto.response;

import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.details.ProfileImageDetails;
import org.demo.server.module.review.dto.details.ReviewImageDetails;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long memberId;
    private String writer;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private Integer star;
    private Long hits;
    private Long goodCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProfileImageDetails profileImageDetails;
    private List<ReviewImageDetails> reviewImagesDetailsList;
}
