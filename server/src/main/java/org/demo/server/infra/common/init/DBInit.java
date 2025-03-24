package org.demo.server.infra.common.init;

import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.service.base.ReviewService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            ReviewSaveForm review1 = new ReviewSaveForm();
            review1.setWriter(adminUsername);
            review1.setTitle("Title1");
            review1.setContent("Content1");
            review1.setStoreName("Store1");
            review1.setStoreAddress("Address1");
            review1.setStar(1);
            reviewService.save(review1);

            ReviewSaveForm review2 = new ReviewSaveForm();
            review2.setWriter(userUsername);
            review2.setTitle("Title2");
            review2.setContent("Content2");
            review2.setStoreName("Store2");
            review2.setStoreAddress("Address2");
            review2.setStar(2);
            reviewService.save(review2);

            ReviewSaveForm review3 = new ReviewSaveForm();
            review3.setWriter(adminUsername);
            review3.setTitle("Title3");
            review3.setContent("Content3");
            review3.setStoreName("Store3");
            review3.setStoreAddress("Address3");
            review3.setStar(3);
            reviewService.save(review3);
        };
    }
}
