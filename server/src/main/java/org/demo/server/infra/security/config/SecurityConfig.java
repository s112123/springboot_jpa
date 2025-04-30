package org.demo.server.infra.security.config;

import org.demo.server.infra.security.filter.AccessTokenCheckFilter;
import org.demo.server.infra.security.filter.LoginFilter;
import org.demo.server.infra.security.filter.RefreshTokenCheckFilter;
import org.demo.server.infra.security.util.JwtUtils;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtils jwtUtils;
    private final MemberFinder memberFinder;
    private final RedisTemplate<String, String> redisTemplate;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            JwtUtils jwtUtils,
            MemberFinder memberFinder,
            @Qualifier("redisTemplate01") RedisTemplate<String, String> redisTemplate
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtils = jwtUtils;
        this.memberFinder = memberFinder;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 시큐리티 필터
        LoginFilter loginFilter = new LoginFilter("/api/v1/login", jwtUtils, memberFinder, redisTemplate);
        loginFilter.setAuthenticationManager(authenticationManager());
        AccessTokenCheckFilter accessTokenCheckFilter = new AccessTokenCheckFilter(jwtUtils);
        RefreshTokenCheckFilter refreshTokenCheckFilter =
                new RefreshTokenCheckFilter("/api/v1/tokens/refresh", jwtUtils, redisTemplate);

        // 시큐리티 설정
        http
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(refreshTokenCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(accessTokenCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // 경로 권한 설정
        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/pages/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/content-images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/profile-images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/sse/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/codes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/emails/check").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/send-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tokens/refresh").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost:9090"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(1000L * 60 * 60 * 24);
        corsConfiguration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/favicon.ico", "/css/**", "/js/**"
        );
    }
}
