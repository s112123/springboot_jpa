package org.demo.server.module.member.util.send.impl.confirm.impl;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.util.send.impl.confirm.base.SendConfirmCode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile(value = "dev")
public class SendConfirmCodeConsole extends SendConfirmCode {

    public SendConfirmCodeConsole(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 인증 코드를 콘솔 창으로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     */
    @Override
    public void send(String to) {
        // 인증 코드
        String code = UUID.randomUUID().toString().substring(0, 6);
        save(to, code);
        log.info("api={}, email={}, code={}", "http://localhost:8081/api/v1/members/codes/check", to, code);
    }
}
