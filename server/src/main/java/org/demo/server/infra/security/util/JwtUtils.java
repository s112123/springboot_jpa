package org.demo.server.infra.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    private final Key key;

    public JwtUtils(@Value("${jwt.secret.key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 유효 시간이 15분인 JWT 생성
     *
     * @param claims username 과 roles 가 포함된 페이로드
     * @return JWT
     */
    public String create(Claims claims) {
        return create(claims, 15);
    }

    /**
     * username 과 authorities 를 담아서 JWT 를 생성한다
     *
     * @param claims username 과 roles 가 포함된 페이로드
     * @param minute JWT 의 만료 기간 (분 단위)
     * @return JWT
     */
    public String create(Claims claims, int minute) {
        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT", "alg", "HS256"))
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * minute)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 검증
     *
     * @param token 검증할 JWT
     * @return JWT 의 페이로드
     */
    public Claims validate(String token) {
        return getJwtParser(key)
                .parseClaimsJws(token)
                .getBody();
   }

    /**
     * SecurityContextHolder 에 저장할 인증된 Authentication
     *
     * @param claims Authentication 에 저장할 인증 정보
     * @return 인증된 Authentication
     */
    public Authentication getAuthentication(Claims claims) {
        String username = (String) claims.get("username");
        List<SimpleGrantedAuthority> roles = ((List<String>) claims.get("roles")).stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(username, null, roles);
    }

    /**
     * JWT Parser 생성
     *
     * @param key Parser 생성을 위한 Key
     * @return JWT Parser
     */
   public JwtParser getJwtParser(Key key) {
       return Jwts.parserBuilder()
               .setSigningKey(key)
               .build();
   }
}
