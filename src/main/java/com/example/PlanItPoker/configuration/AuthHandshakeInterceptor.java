package com.example.PlanItPoker.configuration;

import com.example.PlanItPoker.service.JWTService;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;

    public AuthHandshakeInterceptor(JwtUtil jwtUtil, UserServiceImpl userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extrage token-ul JWT din query param sau header
        String tokenHeader = request.getHeaders().getFirst("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            try {
                UUID userId = jwtUtil.extractUserId(token);
                UserDetails userDetails = userService.loadUserByUserId(userId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Setează autentificarea ca Principal
                attributes.put("user", auth);
            } catch (Exception e) {
                e.printStackTrace(); // Sau log
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // nimic
    }
}
