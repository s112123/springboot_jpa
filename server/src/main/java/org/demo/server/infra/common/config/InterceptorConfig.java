package org.demo.server.infra.common.config;

import org.demo.server.infra.common.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    /* 로그인 확인에서 제외할 엔드 포인트
    private static List<String> excludePathsOfAuthentication = new ArrayList<>(Arrays.asList(
            "/css/**", "/js/**", "/upload/images/member/**", "/upload/images/review/**", "/upload/images/map/**",
            "/", "/error", "/login", "/logout",
            "/reviews", "/review/view",
            "/member/add", "/members", "/members/send_email", "/members/exists_email", "/member/valid_email",
            "/mail/**"
    ));
    */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로그 출력
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**");

        /* 로그인 체크
        registry.addInterceptor(new AuthenticateInterceptor())
                .order(2)
                .excludePathPatterns(excludePathsOfAuthentication);
         */
    }
}
