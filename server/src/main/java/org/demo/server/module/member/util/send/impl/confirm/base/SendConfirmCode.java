package org.demo.server.module.member.util.send.impl.confirm.base;

import org.demo.server.module.member.exception.InvalidVerificationCodeException;
import org.demo.server.module.member.util.send.base.SendUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

public abstract class SendConfirmCode implements SendUtils {

    protected final RedisTemplate<String, String> redisTemplate;

    public SendConfirmCode(
            @Qualifier("redisTemplate00") RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Redis 에 인증 코드 저장
     *
     * @param email Redis Key 에 포함할 email
     * @param code Key 의 Value 로 사용
     */
    protected void save(String email, String code) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(generateKey(email), code, 60L, TimeUnit.SECONDS);
    }

    /**
     * Redis 에 저장된 인증 코드와 전송된 인증 코드를 전송하여 유효한 사용자인지 확인
     *
     * @param email 인증 코드를 보낸 이메일 주소
     * @param code 보낸 인증 코드
     */
    public void validate(String email, String code) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String storedCode = operations.get(generateKey(email));
        if (!code.equals(storedCode)) {
            throw new InvalidVerificationCodeException("유효하지 않은 인증 코드입니다");
        }
    }

    /**
     * Redis 에 인증 코드 저장을 위한 Key 생성
     *
     * @param email 인증 코드를 보낸 이메일 주소
     * @return Redis Key
     */
    private String generateKey(String email) {
        return "join:" + email;
    }
}
