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
        //Check if customer already exist
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return null;
        }

        User user = new User();
        BeanUtils.copyProperties(signupRequest,user);

        //Hash the password before saving
        String hashPassword = passwordEncoder.encode(signupRequest.getPassword());
        user.setPassword(hashPassword);
        User createdCustomer = userRepository.save(user);
        user.setId(createdCustomer.getId());
        return user;
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
