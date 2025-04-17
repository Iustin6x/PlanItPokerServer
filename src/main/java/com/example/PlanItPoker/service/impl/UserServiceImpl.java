package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.payload.request.UserProfileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import com.example.PlanItPoker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class UserServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;


    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("loadUserByUsername"+username);
        com.example.PlanItPoker.model.User user;
        if (username.contains("@")) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        } else {
            try {
                UUID userId = UUID.fromString(username);
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));
            } catch (IllegalArgumentException e) {
                throw new UsernameNotFoundException("Invalid username format: " + username);
            }
        }

        // Check if password is null or empty
        String password = user.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be null or empty");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail() != null ? user.getEmail() : user.getId().toString(),
                password,
                Collections.emptyList());
    }

    public UserDetails loadUserByUserId(UUID userId) {
        com.example.PlanItPoker.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                userId.toString(), // Folosește ID-ul ca username
                user.getPassword(),
                Collections.emptyList()
        );
    }
    public UserDetails loadUserByUsserId(UUID userId) throws UsernameNotFoundException {
        com.example.PlanItPoker.model.User user;

            try {
                user = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
            } catch (IllegalArgumentException e) {
                throw new UsernameNotFoundException("Invalid username format: " + userId);
            }


        // Check if password is null or empty
        String password = user.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be null or empty");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail() != null ? user.getEmail() : user.getId().toString(),
                password,
                Collections.emptyList());
    }

    public com.example.PlanItPoker.model.User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public com.example.PlanItPoker.model.User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public com.example.PlanItPoker.model.User updateName(UUID userId, String newName) {
        com.example.PlanItPoker.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setName(newName);
        return userRepository.save(user);
    }


}