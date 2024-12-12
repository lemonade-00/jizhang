package ntou.cs.project.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ntou.cs.project.Util.*;
import ntou.cs.project.repository.*;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authToken = request.getHeader("Authorization");
        String headerPrefix = "Bearer ";
        if (authToken != null && authToken.startsWith(headerPrefix)) {
            try {
                // 移除 Bearer_ <- _是空格
                authToken = authToken.substring(headerPrefix.length());

                jwtUtil.validateToken(authToken);

                // 驗證成功 透過識別值取得當前使用者資料
                String userID = jwtUtil.getIDFromToken(authToken, "userId");
                // 取得當前使用者
                UserDetails userDetails = userRepository.getUserByID(userID);

                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception ex) {
                SecurityContextHolder.getContext().setAuthentication(null);
            }

        }
        filterChain.doFilter(request, response);

    }
}
