package org.demo.server.infra.common.init;

import org.demo.server.module.follow.repository.FollowRepository;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.entity.Role;
import org.demo.server.module.member.service.base.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class DBInit {

    @Bean
    public CommandLineRunner init(MemberService memberService, FollowRepository followRepository) {
        return args -> {
            // 회원 등록
            MemberSaveForm admin = new MemberSaveForm();
            admin.setEmail("admin@test.com");
            admin.setPassword("a123412341234");
            MemberDetails adminDetails = memberService.save(admin);

            MemberSaveForm user1 = new MemberSaveForm();
            user1.setEmail("user1@test.com");
            user1.setPassword("a123412341234");
            MemberDetails user1Details = memberService.save(user1);

            MemberSaveForm user2 = new MemberSaveForm();
            user2.setEmail("user2@test.com");
            user2.setPassword("a123412341234");
            MemberDetails user2Details = memberService.save(user2);

            MemberSaveForm user3 = new MemberSaveForm();
            user3.setEmail("user3@test.com");
            user3.setPassword("a123412341234");
            MemberDetails user3Details = memberService.save(user3);

            // 권한 변경
            memberService.updateRole(adminDetails.getMemberId(), Role.ADMIN);
        };
    }
}
