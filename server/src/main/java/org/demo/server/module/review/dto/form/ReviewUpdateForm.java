package org.demo.server.module.review.dto.form;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ReviewUpdateForm {

    private Long reviewId;
    private String writer;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private Integer star;
    private List<ReviewImageForm> savedImageForms;
    private List<ReviewImageForm> deletedImageForms;
}