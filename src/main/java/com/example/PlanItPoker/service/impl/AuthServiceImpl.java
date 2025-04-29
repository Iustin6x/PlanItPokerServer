package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.request.SignupRequest;
import com.example.PlanItPoker.repository.UserRepository;
import com.example.PlanItPoker.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User createUser(SignupRequest signupRequest) {
        // Verifică dacă există guest user pentru conversie
        if(signupRequest.getGuestUserId() != null) {
            User guestUser = userRepository.findById(signupRequest.getGuestUserId())
                    .orElseThrow(() -> new RuntimeException("Guest user not found"));

            if(!guestUser.isGuest()) {
                throw new RuntimeException("User is already registered");
            }

            // Verifică unicătatea email-ului
            if(userRepository.existsByEmail(signupRequest.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }

            // Actualizează guest user-ul existent
            guestUser.setEmail(signupRequest.getEmail());
            guestUser.setName(signupRequest.getName());
            guestUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            guestUser.setGuest(false);

            return userRepository.save(guestUser);
        }

        // Cazul pentru înregistrare nouă normală
        if(userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User newUser = new User();
        newUser.setEmail(signupRequest.getEmail());
        newUser.setName(signupRequest.getName());
        newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        newUser.setGuest(false);

        return userRepository.save(newUser);
    }

    @Override
    public User createGuestUser(String name) {
        User guestUser = new User();
        guestUser.setName(name);
        guestUser.setGuest(true);
        guestUser.setPassword(passwordEncoder.encode("guest_dummy_password"));
        // email și password rămân null
        return userRepository.save(guestUser);
    }
}
