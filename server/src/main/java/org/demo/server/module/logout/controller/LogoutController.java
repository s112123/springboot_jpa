package org.demo.server.module.logout.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.demo.server.infra.sse.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logout")
public class LogoutController {

    private final SseEmitterService sseEmitterService;
    private final RedisTemplate<String, String> redisTemplate;

    public LogoutController(
            SseEmitterService sseEmitterService,
            @Qualifier("redisTemplate01") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterService = sseEmitterService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public ResponseEntity<Void> logout(
            @RequestBody Long memberId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // SseEmitter 삭제
        sseEmitterService.removeEmitter(memberId);
        // Redis 에서 Refresh Token 제거
        redisTemplate.delete("refreshToken:member:" + memberId);

        // 쿠키 제거
        Cookie refreshTokenCookie = getRefreshTokenCooke(request);
        if (refreshTokenCookie != null) {
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);
        }

        return ResponseEntity.ok().build();
    }

    // RefreshToken 쿠키 가져오기
    private Cookie getRefreshTokenCooke(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("todayReviewsRefreshToken")) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
