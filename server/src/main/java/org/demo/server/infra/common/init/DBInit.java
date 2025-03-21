package org.demo.server.infra.common.init;

import org.demo.server.module.member.dto.request.MemberSaveRequest;
import org.demo.server.module.member.service.base.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBInit {

    // 최초 관리자 등록
    @Bean
    public CommandLineRunner init(MemberService memberService) {
        return args -> {
            MemberSaveRequest memberSaveRequest = new MemberSaveRequest();
            memberSaveRequest.setEmail("admin@test.com");
            memberSaveRequest.setPassword("a123412341234");
            memberService.save(memberSaveRequest);
        };
    }
}
