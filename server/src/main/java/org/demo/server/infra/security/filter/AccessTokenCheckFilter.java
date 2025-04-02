package org.demo.server.infra.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.security.util.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AccessTokenCheckFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private List<String> whiteList = List.of(
            "/api/v1/login",
            "/api/v1/members/send-password",
            "/ws/info"
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
        log.info("requestURI={}", requestURI);
        if (whiteList.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Access Token 가져오기
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String accessToken = token.substring(7);
            Claims claims = jwtUtils.validate(accessToken);

            // 인증 정보 저장
            Authentication authentication = jwtUtils.getAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
