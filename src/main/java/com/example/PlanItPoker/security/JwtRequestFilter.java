package com.example.PlanItPoker.security;

import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final UserServiceImpl userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRequestFilter(UserServiceImpl userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("doFilterInternal");
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] parts = authHeader.split("\\s+");
            if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer")) {
                logger.warn("Authorization header is incorrect");
                filterChain.doFilter(request, response);
                return;
            }

            String token = parts[1];
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);
                logger.info("username {} logged in", username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    if (!userDetails.isEnabled()) {
                        throw new Exception("User account is disabled");
                    }
                    if (!userDetails.isAccountNonExpired()) {
                        throw new Exception("User account has expired");
                    }
                    if (!userDetails.isAccountNonLocked()) {
                        throw new Exception("User account is locked");
                    }
                    if (!userDetails.isCredentialsNonExpired()) {
                        throw new Exception("User credentials have expired");
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Authentication failed: ", ex);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authentication failed: " + ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}