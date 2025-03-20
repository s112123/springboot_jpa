package org.demo.server.infra.common.util.send.impl;

import org.demo.server.infra.common.util.send.base.SendUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile(value = "dev")
public class SendConsoleUtils extends SendUtils {

    /**
     * 인증 코드 (6자리) 를 콘솔 창으로 발송
     *
     * @param to 인증 코드를 보낼 대상
     * @return 인증 코드
     */
    @Override
    public String sendConfirmationCode(String to) {
        // 인증 코드
        String code = UUID.randomUUID().toString().substring(0, 6);

        // Redis 에 저장


        return code;
    }
}
