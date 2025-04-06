package com.example.PlanItPoker.service.impl;

import org.springframework.security.core.userdetails.User;
import com.example.PlanItPoker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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

    public com.example.PlanItPoker.model.User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public com.example.PlanItPoker.model.User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}