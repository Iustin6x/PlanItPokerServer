package com.example.PlanItPoker.configuration;

import com.example.PlanItPoker.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        try {
            String token = extractToken(request);
            UUID userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid user ID in token");
            }

            attributes.put("userId", userId.toString());
            return true;

        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Authentication failed", e);
        }
    }

    private String extractToken(ServerHttpRequest request) {
        URI uri = request.getURI();
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

        String token = params.getFirst("token");
        if (token != null) return token;

        throw new AuthenticationCredentialsNotFoundException("Token not found");
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}