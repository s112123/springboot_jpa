package org.demo.server.infra.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.security.authentication.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    // 컨트롤러 요청 전
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws Exception {
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                String username = ((CustomUserDetails) principal).getUsername();
                log.info("request: principal={}, httpMethod={}, requestURI={}, handler={}",
                        principal, httpMethod, requestURI, handler);
            }
        }

        log.info("request: httpMethod={}, requestURI={}, handler={}", httpMethod, requestURI, handler);
        return true;
    }

    // 뷰 렌더링 후 (무조건 호출)
    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex
    ) throws Exception {
        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        log.info("response: httpMethod={}, requestURI={}, handler={}", httpMethod, requestURI, handler);
    }
}
