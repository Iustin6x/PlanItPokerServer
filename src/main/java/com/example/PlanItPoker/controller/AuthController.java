package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.request.QuickplayRequest;
import com.example.PlanItPoker.service.AuthService;
import com.example.PlanItPoker.payload.request.LoginRequest;
import com.example.PlanItPoker.payload.request.SignupRequest;
import com.example.PlanItPoker.payload.response.LoginResponse;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final AuthenticationManager authenticationManager;

    private final UserServiceImpl userService;

    private final AuthService authService;

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, AuthService authService, UserServiceImpl userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws IOException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect email or password.");
            return null;
        } catch (DisabledException disabledException) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User is not activated");
            return null;
        }
        com.example.PlanItPoker.model.User user = userService.findByEmail(loginRequest.getEmail());
        final String jwt = jwtUtil.generateToken(user);
        return new LoginResponse(jwt);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody SignupRequest signupRequest) {
        try {
            User createdUser = authService.createUser(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @PostMapping("/quickplay")
    public ResponseEntity<?> quickplay(@RequestBody QuickplayRequest request) {
        try {
            com.example.PlanItPoker.model.User guestUser = authService.createGuestUser(request.getName());
            String jwt = jwtUtil.generateToken(guestUser);
            return ResponseEntity.ok(new LoginResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating guest user");
        }
    }

    @GetMapping("/api/user")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.extractUserId(token);
        com.example.PlanItPoker.model.User user = userService.findById(userId);
        logger.info("/user",user.getId());
        return ResponseEntity.ok(user);
    }




}
