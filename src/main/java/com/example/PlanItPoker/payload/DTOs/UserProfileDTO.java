package com.example.PlanItPoker.payload.DTOs;

public record UserProfileDTO(
        String name,
        String avatar,
        String email,
        boolean isGuest
) {}
