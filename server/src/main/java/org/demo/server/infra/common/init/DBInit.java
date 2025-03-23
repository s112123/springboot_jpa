package org.demo.server.infra.common.init;

import org.demo.server.module.member.dto.request.MemberSaveRequest;
import org.demo.server.module.member.service.base.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBInit {

    @Bean
    public CommandLineRunner init(MemberService memberService) {
        return args -> {
            MemberSaveRequest admin = new MemberSaveRequest();
            admin.setEmail("admin@test.com");
            admin.setPassword("a123412341234");
            memberService.save(admin);

            MemberSaveRequest user = new MemberSaveRequest();
            user.setEmail("user@test.com");
            user.setPassword("a123412341234");
            memberService.save(user);
        };
    }
}
