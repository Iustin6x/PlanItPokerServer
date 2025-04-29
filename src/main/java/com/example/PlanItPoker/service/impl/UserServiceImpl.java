package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
        logger.info("Attempting to load user by username/email: {}", username);
        User user;

        if (username.contains("@")) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found with email: {}", username);
                        return new UsernameNotFoundException("User not found with email: " + username);
                    });
        } else {
            try {
                UUID userId = UUID.fromString(username);
                user = userRepository.findById(userId)
                        .orElseThrow(() -> {
                            logger.warn("User not found with ID: {}", username);
                            return new UsernameNotFoundException("User not found with id: " + username);
                        });
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format: {}", username);
                throw new UsernameNotFoundException("Invalid username format: " + username);
            }
        }

        validatePassword(user);

        logger.info("User loaded successfully: {}", user.getId());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail() != null ? user.getEmail() : user.getId().toString(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    public UserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        logger.info("Attempting to load user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with id: " + userId);
                });

        validatePassword(user);

        logger.info("User loaded by ID successfully: {}", userId);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail() != null ? user.getEmail() : userId.toString(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    public User findByEmail(String email) {
        logger.info("Finding user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    public User findById(UUID id) {
        logger.info("Finding user by ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });
    }

    public User updateName(UUID userId, String newName) {
        logger.info("Updating name for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        user.setName(newName);
        logger.info("User name updated for ID: {}", userId);
        return userRepository.save(user);
    }

    private void validatePassword(User user) {
        String password = user.getPassword();
        if (password == null || password.trim().isEmpty()) {
            logger.error("User {} has null or empty password", user.getId());
            throw new IllegalArgumentException("User password cannot be null or empty");
        }
    }
}
