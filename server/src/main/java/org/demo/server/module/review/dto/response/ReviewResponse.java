package org.demo.server.module.review.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {

    private long reviewId;
    private String writer;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private int star;
    private long hits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
