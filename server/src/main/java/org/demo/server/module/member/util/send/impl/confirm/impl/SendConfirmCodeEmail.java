package org.demo.server.module.member.util.send.impl.confirm.impl;

import org.demo.server.module.member.util.send.impl.confirm.base.SendConfirmCode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile(value = "prod")
public class SendConfirmCodeEmail extends SendConfirmCode {

    public SendConfirmCodeEmail(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 인증 코드를 콘솔 창으로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     */
    @Override
    public void send(String to) {
    }
}
