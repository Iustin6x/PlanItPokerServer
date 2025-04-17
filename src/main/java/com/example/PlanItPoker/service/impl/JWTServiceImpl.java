package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.service.JWTService;
import com.example.PlanItPoker.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.UUID;

@Service
@Transactional
public class JWTServiceImpl implements JWTService {
    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;

    public JWTServiceImpl(JwtUtil jwtUtil, UserServiceImpl userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public String generateJwt(String username) throws ParseException {
        return "";
    }

    @Override
    public Authentication validateJwt(String jwt) {
        return null;
    }

    @Override
    public Authentication validateToken(String token) {
        logger.info("Validating token: " + token);
        try {

            UUID userId = jwtUtil.extractUserId(token);
            UserDetails userDetails = userService.loadUserByUsername(userId.toString());

            if (jwtUtil.validateToken(token, userDetails)) {
                return new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
            }
        } catch (Exception e) {
            // Loghează eroarea dacă este necesar
        }
        return null;
    }
}
