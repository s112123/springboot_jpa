package org.demo.server.module.review.dto.details;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageDetails {

    private String originalFileName;
    private String savedFileName;
    private String path;
}
