package org.demo.server.infra.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AuthenticateInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws Exception {
        /*
        HttpSession session = request.getSession();
        if (session == null || session.getAttribute("email") == null) {
            response.sendRedirect("/login");
            return false;
        }
         */
        return true;
    }
}
