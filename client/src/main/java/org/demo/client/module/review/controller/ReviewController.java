package org.demo.client.module.review.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/review")
public class ReviewController {

    /**
     * 리뷰 등록 페이지
     *
     * @return
     */
    @GetMapping("/add")
    public String addReviewPage() {
        return "review/add";
    }

    /**
     * 리뷰 보기 페이지
     *
     * @return
     */
    @GetMapping("/view")
    public String viewReviewPage() {
        return "review/view";
    }

    /**
     * 리뷰 수정 페이지
     *
     * @return
     */
    @GetMapping("/edit")
    public String editReviewPage() {
        return "review/edit";
    }
}
