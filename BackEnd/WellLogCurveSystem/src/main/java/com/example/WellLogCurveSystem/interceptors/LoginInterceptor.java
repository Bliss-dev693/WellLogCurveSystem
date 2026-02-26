package com.example.WellLogCurveSystem.interceptors;

import com.example.WellLogCurveSystem.utils.JwtUtil;
import com.example.WellLogCurveSystem.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401); // 未授权
            response.getWriter().write("Unauthorized: Missing token");
            return false;
        } else {
            // 解析JWT
            try {
                Claims claims = JwtUtil.parseToken(token);
                //把业务数据存储到threadLocal中
                ThreadLocalUtil.set(claims);
                return true;
            } catch (Exception e) {
                response.setStatus(401); // 未授权
                response.getWriter().write("Unauthorized: Invalid token");
                return false;
            }
        }

    }
   @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除线程变量中的数据，避免内存泄漏
        ThreadLocalUtil.remove();
    }

}