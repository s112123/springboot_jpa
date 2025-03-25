package org.demo.server.infra.common.init;

import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.review.dto.form.ReviewImageForm;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.service.base.ReviewService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DBInit {

    @Bean
    public CommandLineRunner init(MemberService memberService, ReviewService reviewService) {
        return args -> {
            // 회원 등록
            MemberSaveForm admin = new MemberSaveForm();
            admin.setEmail("admin@test.com");
            admin.setPassword("a123412341234");
            MemberDetails savedAdmin = memberService.save(admin);

            MemberSaveForm user = new MemberSaveForm();
            user.setEmail("user@test.com");
            user.setPassword("a123412341234");
            MemberDetails savedUser = memberService.save(user);

            // 회원 정보 아이디
            String adminUsername = memberService.findById(savedAdmin.getMemberId()).getUsername();
            String userUsername = memberService.findById(savedUser.getMemberId()).getUsername();

            // 리뷰 등록
//            List<ReviewImageForm> reviewImageForms = new ArrayList<>();
//            ReviewImageForm reviewImageForm1 = ReviewImageForm.builder()
//                    .originalFileName("lenna1.png")
//                    .savedFileName("lenna1.png")
//                    .build();
//            reviewImageForms.add(reviewImageForm1);
//
//            ReviewImageForm reviewImageForm2 = ReviewImageForm.builder()
//                    .originalFileName("lenna2.png")
//                    .savedFileName("lenna2.png")
//                    .build();
//            reviewImageForms.add(reviewImageForm2);
//
//            ReviewSaveForm reviewSaveForm = new ReviewSaveForm();
//            reviewSaveForm.setWriter(adminUsername);
//            reviewSaveForm.setTitle("Title1");
//            reviewSaveForm.setContent("Content1");
//            reviewSaveForm.setStoreName("Store1");
//            reviewSaveForm.setStoreAddress("Address1");
//            reviewSaveForm.setStar(1);
//            reviewSaveForm.setReviewImageForms(reviewImageForms);
//            reviewService.save(reviewSaveForm);
        };
    }
}
