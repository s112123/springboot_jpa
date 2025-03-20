package org.demo.server.module.member.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.exception.InvalidVerificationCodeException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate;

    public EmailService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Redis 에 회원 가입을 위한 이메일로 인증 코드 저장
     *
     * @param email 인증 코드를 보낼 이메일 주소
     * @param code 인증 코드
     */
    public void saveConfirmationCode(String email, String code) {
        // key → join:이메일주소
        // value → 인증 코드
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("join:" + email, code, 60L, TimeUnit.SECONDS);
    }

    /**
     * Redis 에 저장된 인증 코드와 이메일로 전송된 인증 코드를 전송하여 유효한 사용자인지 확인
     *
     * @param email 인증 코드를 보낸 이메일 주소
     * @param code 보낸 인증 코드
     */
    public void confirmCode(String email, String code) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String storedCode = operations.get("join:" + email);
        if (!code.equals(storedCode)) {
            throw new InvalidVerificationCodeException("유효하지 않은 인증 코드입니다");
        }
    }
}
