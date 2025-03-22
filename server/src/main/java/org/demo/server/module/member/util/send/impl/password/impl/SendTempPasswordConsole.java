package org.demo.server.module.member.util.send.impl.password.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.member.util.send.impl.password.base.SendTempPassword;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(value = "dev")
public class SendTempPasswordConsole extends SendTempPassword {

    private final MemberService memberService;
    
    /**
     * 임시 비밀번호를 콘솔 창으로 발송하고 비밀번호 변경
     *
     * @param to 인증 코드를 보낼 대상
     */
    @Override
    public void send(String to) {
        // 임시 비밀번호 (20자리)
        String tempPassword = UUID.randomUUID().toString().substring(0, 20);
        // 회원 비밀번호를 임시 비밀번호로 변경
        log.info("비밀번호 변경");
        // 임시 비밀번호 발송
        log.info("tempPassword={}", tempPassword);
    }
}
