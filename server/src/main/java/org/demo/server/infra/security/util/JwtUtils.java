package org.demo.server.infra.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final Key key;

    public JwtUtils(@Value("${jwt.secret.key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * username 과 authorities 를 담아서 JWT 를 생성한다
     *
     * @param claims username 과 authorities 가 포함된 페이로드
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
}
