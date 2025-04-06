package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.request.SignupRequest;

public interface AuthService {
    User createUser(SignupRequest signupRequest);
    User createGuestUser(String name);
}
