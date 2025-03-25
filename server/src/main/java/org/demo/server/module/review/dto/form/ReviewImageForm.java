package org.demo.server.module.review.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.demo.server.module.review.entity.Review;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageForm {

    private String originalFileName;
    private String savedFileName;
}
