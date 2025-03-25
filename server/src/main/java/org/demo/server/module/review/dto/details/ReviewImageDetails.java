package org.demo.server.module.review.dto.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewImageDetails {

    private Long reviewImageId;
    private String originalFileName;
    private String savedFileName;
    @JsonProperty("isThumbnail")
    private boolean isThumbnail;
}
