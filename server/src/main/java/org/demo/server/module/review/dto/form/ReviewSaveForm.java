package org.demo.server.module.review.dto.form;

import lombok.Data;

import java.util.List;

@Data
public class ReviewSaveForm {

    private String writer;
    private String title;
    private String content;
    private String storeName;
    private String storeAddress;
    private Integer star;
    private List<ReviewImageForm> reviewImageForms;
}