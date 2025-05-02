package org.demo.server.infra.common.init;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.entity.Role;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.member.service.base.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DBInit {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberFinder memberFinder;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            // 회원 등록
            try {
                memberFinder.getMemberByEmail("admin@test.com");
            } catch (NotFoundException e) {
                MemberSaveForm admin = new MemberSaveForm();
                admin.setEmail("admin@test.com");
                admin.setPassword("a123412341234");
                MemberDetails savedMember = memberService.save(admin);
                // 권한 변경
                memberService.updateRole(savedMember.getMemberId(), Role.ADMIN);
            }

            try {
                memberFinder.getMemberByEmail("user1@test.com");
            } catch (NotFoundException e) {
                MemberSaveForm user1 = new MemberSaveForm();
                user1.setEmail("user1@test.com");
                user1.setPassword("a123412341234");
                memberService.save(user1);
            }

            try {
                memberFinder.getMemberByEmail("user2@test.com");
            } catch (NotFoundException e) {
                MemberSaveForm user2 = new MemberSaveForm();
                user2.setEmail("user2@test.com");
                user2.setPassword("a123412341234");
                memberService.save(user2);
            }

            try {
                memberFinder.getMemberByEmail("user3@test.com");
            } catch (NotFoundException e) {
                MemberSaveForm user3 = new MemberSaveForm();
                user3.setEmail("user3@test.com");
                user3.setPassword("a123412341234");
                memberService.save(user3);
            }

            log.info("Server Initialization Completed.");
        };
    }
}
