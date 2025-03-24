package org.demo.server.module.review.dto.form;

import lombok.Data;
import lombok.ToString;
import org.demo.server.module.review.dto.details.ReviewImageDetails;

import java.util.List;

@Data
@ToString
public class ReviewSaveForm {

    private String writer;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private Integer star;
    private List<ReviewImageDetails> reviewImages;
//    private String originalFileName;
//    private String savedFileName;
//    private String path;
}