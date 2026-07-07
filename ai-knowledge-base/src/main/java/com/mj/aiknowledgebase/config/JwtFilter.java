package com.mj.aiknowledgebase.config;

import com.mj.aiknowledgebase.util.JwtUtil;
import com.mj.aiknowledgebase.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserContext userContext;

    private static final String[] WHITE_LIST = {
            "/user/register",
            "/user/login",
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/swagger-resources"
    };
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

//        白名单放行
        for (String whitePath : WHITE_LIST){
            if(path.startsWith(whitePath)){
                filterChain.doFilter(request, response);
                return;
            }
        }

//        从Header中取Token
        String authHeader = request.getHeader("Authorization");

        if ( authHeader == null || ! authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            response.getWriter().write("Token is null or invalid");
            response.setContentType("application/json");
            return;
        }
        String token = authHeader.substring(7);

        try{
//            解析Token,取出userId
            Long userId = jwtUtil.getUserIdFromToken(token);
//            存入 request 后续Controller通过request.getAttribute("userId")获取获取
            UserContext.setUserId(userId);
            request.setAttribute("userId", userId);
            filterChain.doFilter(request, response);
        }catch (Exception e){
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\",\"data\":null}");
            response.setContentType("application/json");
            return;
        }
    }
}
