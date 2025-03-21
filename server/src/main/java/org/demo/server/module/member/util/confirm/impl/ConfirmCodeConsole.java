package org.demo.server.module.member.util.confirm.impl;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.util.confirm.base.ConfirmCodeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Profile(value = "dev")
public class ConfirmCodeConsole extends ConfirmCodeUtils {

    public ConfirmCodeConsole(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 인증 코드를 콘솔 창으로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     * @return 인증 코드
     */
    @Override
    public void sendConfirmCode(String to) {
        // 인증 코드
        String code = UUID.randomUUID().toString().substring(0, 6);
        saveConfirmCode(to, code);
        log.info("api={}, email={}, code={}", "http://localhost:8081/api/v1/members/codes/check", to, code);
    }
}
