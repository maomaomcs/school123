package com.school.site.config;

import com.school.site.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminAuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String token = request.getHeader("X-Admin-Token");
        if (token == null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        }
        if (authService.valid(token)) {
            request.setAttribute("adminUser", authService.usernameOf(token));
            return true;
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"未登录或登录已过期\"}");
        return false;
    }
}
