package com.mj.aiknowledgebase.interceptor;

import com.mj.aiknowledgebase.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
            try{
                Long userId = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
            }catch (Exception e){
                response.setStatus(401);
                response.getWriter().write("Token is null or invalid");
                response.setContentType("application/json");
                return false;
            }
        }else {
            response.setStatus(401);
            response.getWriter().write("Token is null or invalid");
            response.setContentType("application/json");
            return false;
        }
        return true;
    }
}
