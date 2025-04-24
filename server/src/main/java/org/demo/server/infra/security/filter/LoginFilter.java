package org.demo.server.infra.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.security.constant.TokenExpiration;
import org.demo.server.infra.security.dto.LoginRequest;
import org.demo.server.infra.security.util.JwtUtils;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtUtils jwtUtils;
    private final MemberFinder memberFinder;
    private final RedisTemplate<String, String> redisTemplate;

    public LoginFilter(
            String defaultFilterProcessesUrl,
            JwtUtils jwtUtils,
            MemberFinder memberFinder,
            RedisTemplate<String, String> redisTemplate
    ) {
        // 로그인 경로
        super(defaultFilterProcessesUrl);
        this.jwtUtils = jwtUtils;
        this.memberFinder = memberFinder;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 로그인 정보로 인증 처리
     *
     * @param request from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     * redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response
    ) throws AuthenticationException, IOException, ServletException {
        // POST 요청인지 검증
        if (!request.getMethod().equals("POST")) {
            log.info("Not POST Method");
            return null;
        }

        // JSON 으로 받은 로그인 정보
        ObjectMapper objectMapper = new ObjectMapper();
        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        // 인증을 위한 인증 정보를 담은 Authentication 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        // 인증 처리 후, 인증된 Authentication 반환
        return getAuthenticationManager().authenticate(authentication);
    }

    /**
     * 인증 성공 시, JSON 전송
     *
     * @param request
     * @param response
     * @param chain
     * @param authentication the object returned from the <tt>attemptAuthentication</tt>
     * method.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authentication
    ) throws IOException, ServletException {
        // JWT 에 담을 사용자 정보 → username, authorities
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList();
        Long memberId = memberFinder.getMemberByUsername(username).getMemberId();

        // JWT 페이로드 생성
        Claims claims = Jwts.claims();
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("memberId", memberId);

        // JWT 생성
        String accessToken = jwtUtils.create(claims, TokenExpiration.ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtUtils.create(claims, TokenExpiration.REFRESH_TOKEN_EXPIRATION);

        // Refresh Token 은 Redis 에 저장
        redisTemplate.opsForValue().set(
                "refreshToken:member:" + memberId, refreshToken,
                TokenExpiration.REFRESH_TOKEN_EXPIRATION
        );
        // Refresh Token 은 쿠키로 저장한다
        Cookie refreshTokenCookie = new Cookie("todayReviewsRefreshToken", refreshToken);
        // 전체 경로에서 유효
        refreshTokenCookie.setPath("/");
        // 유효 기간 3분
        refreshTokenCookie.setMaxAge(60 * TokenExpiration.REFRESH_TOKEN_EXPIRATION);
        // 현재 클라이언트는 8080 포트이고 서버는 8081 포트이므로 쿠키는 다른 도메인으로 간주된다
        // 그래서 클라이언트의 도메인을 허용해주어야 한다 + CORS 설정도 필요
        refreshTokenCookie.setDomain("localhost");
        // true 를 하면 JS 에서 접근 불가 → document.cookie 로 refreshToken 을 볼 수 없다
        refreshTokenCookie.setHttpOnly(false);
        // true 를 하면 HTTPS 에서만 전송 가능
        // SameSite 설정을 강화하려면 Set-Cookie 헤더를 조작하거만 ResponseCookie 를 사용할 수 있다
        refreshTokenCookie.setSecure(false);
        response.addCookie(refreshTokenCookie);

        // Access Token 은 클라이언트에 응답으로 전송하여 클라이언트에서 localStorage 에 저장
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(Map.of("accessToken", accessToken));
        sendResponseJson(response, HttpServletResponse.SC_OK, body);
    }

    /**
     * 인증 실패 시, 401 에러 응답
     *
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException failed
    ) throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(Map.of(
                "status", HttpServletResponse.SC_UNAUTHORIZED,
                "message", "아이디 또는 비밀번호가 잘못되었습니다"
        ));
        sendResponseJson(response, HttpServletResponse.SC_UNAUTHORIZED, body);
    }

    /**
     * Json 으로 응답 보내기
     *
     */
    private void sendResponseJson(HttpServletResponse response, int status, String body) {
        try {
            response.setStatus(status);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
