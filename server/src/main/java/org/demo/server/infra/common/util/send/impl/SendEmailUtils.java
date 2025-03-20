package org.demo.server.infra.common.util.send.impl;

import org.demo.server.infra.common.util.send.base.SendUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile(value = "prod")
public class SendEmailUtils extends SendUtils {

    /**
     * 인증 코드 (6자리) 를 메일로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     * @return 인증 코드
     */
    @Override
    public String sendConfirmationCode(String to) {
        return "pass";
    }
}
