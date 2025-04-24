package org.demo.server.infra.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.demo.server.infra.security.constant.TokenExpiration;
import org.demo.server.infra.security.exception.TokenException;
import org.demo.server.infra.security.exception.TokenStatus;
import org.demo.server.infra.security.util.JwtUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class RefreshTokenCheckFilter extends OncePerRequestFilter {

    private final String requestURLForRefreshAccessToken;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenCheckFilter(
            String requestURLForRefreshAccessToken,
            JwtUtils jwtUtils,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.requestURLForRefreshAccessToken = requestURLForRefreshAccessToken;
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        // POST /api/v1/tokens/refresh 요청인 경우에만 검증을 처리한다
        String requestURI = request.getRequestURI();
        if (!request.getMethod().equals("POST") || !requestURI.equals(requestURLForRefreshAccessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Refresh Token 검증
            Claims refreshTokenClaims = validateRefreshToken(request);

            // 새로운 Access Token 생성
            // AccessToken → JSON 생성
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(Map.of("accessToken", getNewAccessToken(refreshTokenClaims)));

            // Access Token 을 재발급할 때, Refresh Token 도 재발급 한다 → Refresh Token 은 1회성으로 사용
            String newRefreshToken = getNewRefreshToken(refreshTokenClaims);

            // RefreshToken → Redis 에 저장
            long memberId = refreshTokenClaims.get("mid", Integer.class).longValue();
            redisTemplate.opsForValue().set(
                    "refreshToken:member:" + memberId, newRefreshToken,
                    Duration.ofMinutes(TokenExpiration.REFRESH_TOKEN_EXPIRATION)
            );

            // RefreshToken → 브라우저의 쿠키로 전송
            Cookie refreshTokenCookie = new Cookie("todayReviewsRefreshToken", newRefreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * TokenExpiration.REFRESH_TOKEN_EXPIRATION);
            response.addCookie(refreshTokenCookie);

            // AccessToken → 브라우저에서 localStorage 로 저장할 수 있도록 JSON 응답
            // AccessToken 도 브라우저의 쿠키에 담는 것을 권장한다
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(json);
        } catch (TokenException e) {
            e.sendTokenErrorResponse(response);
        }
    }

    // Refresh Token 예외 처리
    private Claims validateRefreshToken(HttpServletRequest request) throws TokenException {
        // Refresh Token 은 Cookie 에서 받는다
        String refreshToken = getRefreshAccessToken(request);
        if (refreshToken == null) {
            throw new TokenException(TokenStatus.NOT_FOUND);
        }

        try {
            // Refresh Token 검증
            Claims refreshTokenClaims = jwtUtils.validate(refreshToken);

            // Refresh Token 이 Redis 에 존재하는 Refresh Token 과 일치하는지 확인한다
            // 유효하지 않은 Token 은 신뢰해서는 안된다
            long memberId = refreshTokenClaims.get("mid", Integer.class).longValue();
            String savedRefreshToken = redisTemplate.opsForValue().get("refreshToken:member:" + memberId);

            if (!savedRefreshToken.equals(refreshToken)) {
                throw new TokenException(TokenStatus.INVALID);
            }

            return refreshTokenClaims;
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenStatus.REFRESH_TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new TokenException(TokenStatus.MALFORMED);
        } catch (SignatureException e) {
            throw new TokenException(TokenStatus.BAD_SIGNATURE);
        }
    }

    // 쿠키에서 Refresh Token 값 추출
    private String getRefreshAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("todayReviewsRefreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 새로운 Access Token 생성
    private String getNewAccessToken(Claims refreshTokenClaims) {
        // 새로운 Access Token 의 Claims 정보
        Claims newAccessTokenClaims = Jwts.claims();
        newAccessTokenClaims.put("username", refreshTokenClaims.get("username"));
        newAccessTokenClaims.put("roles", refreshTokenClaims.get("roles"));
        newAccessTokenClaims.put("mid", refreshTokenClaims.get("mid"));

        // 새로운 Access Token 재발행
        return jwtUtils.create(newAccessTokenClaims, TokenExpiration.ACCESS_TOKEN_EXPIRATION);
    }

    // 새로운 Refresh Token 생성
    private String getNewRefreshToken(Claims refreshTokenClaims) {
        // 새로운 Refresh Token 의 Claims 정보
        Claims newRefreshTokenClaims = Jwts.claims();
        newRefreshTokenClaims.put("username", refreshTokenClaims.get("username"));
        newRefreshTokenClaims.put("roles", refreshTokenClaims.get("roles"));
        newRefreshTokenClaims.put("mid", refreshTokenClaims.get("mid"));

        // 새로운 Refresh Token 재발행
        return jwtUtils.create(newRefreshTokenClaims, TokenExpiration.REFRESH_TOKEN_EXPIRATION);
    }
}
