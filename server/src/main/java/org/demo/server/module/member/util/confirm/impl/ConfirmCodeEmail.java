package org.demo.server.module.member.util.confirm.impl;

import org.demo.server.module.member.util.confirm.base.ConfirmCodeUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "prod")
public class ConfirmCodeEmail extends ConfirmCodeUtils {

    public ConfirmCodeEmail(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 인증 코드 (6자리) 를 메일로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     * @return 인증 코드
     */
    @Override
    public void sendConfirmCode(String to) {
    }
}
