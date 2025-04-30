package org.demo.server.infra.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.security.exception.TokenException;
import org.demo.server.infra.security.exception.TokenStatus;
import org.demo.server.infra.security.util.JwtUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AccessTokenCheckFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private List<String> exactWhiteListForGetMethod = List.of(
            "/",
            "/ws/info"
    );
    private List<String> exactWhiteListForPostMethod = List.of(
            "/api/v1/login",
            "/api/v1/logout",
            "/api/v1/members",
            "/api/v1/members/emails/check",
            "/api/v1/members/send-password",
            "/ws/info",
            "/api/v1/tokens/refresh"
    );
    private List<String> prefixWhiteListForGetMethod = List.of(
            "/api/v1/reviews/",
            "/api/v1/reviews/pages/",
            "/api/v1/reviews/content-images/",
            "/api/v1/members/profile-images/",
            "/api/v1/sse/",
            "/ws/"
    );
    private List<String> prefixWhiteListForPostMethod = List.of(
            "/api/v1/members/codes"
    );

    public AccessTokenCheckFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        // 검증 안할 경로
        String requestURI = request.getRequestURI();
        log.info("[1] requestURI={}", requestURI);
        if (
                isStaticResource(requestURI) ||
                isWhiteListForGetMethod(request, requestURI) ||
                isWhiteListForPostMethod(request, requestURI)
        ) {
            log.info("[2] requestURI={}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Access Token 토큰 검증
            Claims claims = validateAccessToken(request);

            // 인증 정보 저장
            Authentication authentication = jwtUtils.getAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (TokenException e) {
            e.sendTokenErrorResponse(response);
        }
    }

    // 토큰 검증
    private Claims validateAccessToken(HttpServletRequest request) throws TokenException {
        // Authorization 헤더에 있는 토큰 추출
        String token = request.getHeader("Authorization");

        // 토큰이 없는 경우
        if (token == null) {
            throw new TokenException(TokenStatus.NOT_FOUND);
        }

        // 잘못된 토큰인 경우
        if (!token.startsWith("Bearer ")) {
            throw new TokenException(TokenStatus.BAD_TYPE);
        }

        try {
            // Access Token 토큰 검증
            String accessToken = token.substring(7);
            return jwtUtils.validate(accessToken);
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenStatus.ACCESS_TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new TokenException(TokenStatus.MALFORMED);
        } catch (SignatureException e) {
            throw new TokenException(TokenStatus.BAD_SIGNATURE);
        }
    }

    // 정적 리소스인지 확인
    private boolean isStaticResource(String uri) {
        return uri.equals("/favicon.ico") || uri.startsWith("/js/") || uri.startsWith("/css/");
    }

    // Access Token 을 검증하지 않을 경로인지 확인 → GET 요청
    private boolean isWhiteListForGetMethod(HttpServletRequest request, String uri) {
        if (request.getMethod().equals("GET")) {
            return exactWhiteListForGetMethod.contains(uri) ||
                    prefixWhiteListForGetMethod.stream().anyMatch(pattern -> uri.startsWith(pattern));
        }
        return false;
    }

    // Access Token 을 검증하지 않을 경로인지 확인 → POST 요청
    private boolean isWhiteListForPostMethod(HttpServletRequest request, String uri) {
        if (request.getMethod().equals("POST")) {
            return exactWhiteListForPostMethod.contains(uri) ||
                    prefixWhiteListForPostMethod.stream().anyMatch(pattern -> uri.startsWith(pattern));
        }
        return false;
    }
}
