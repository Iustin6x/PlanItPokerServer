package com.example.PlanItPoker.payload.request;

import java.util.UUID;

public record UserProfileRequest(
//        @NotBlank(message = "Name cannot be blank")
        String name,

        String avatar
) {}