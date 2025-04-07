package com.example.PlanItPoker.payload.request;

import java.util.UUID;

public record VoteRequest(
//        @NotNull(message = "Session ID cannot be null")
        UUID sessionId,

//        @NotBlank(message = "Card value cannot be blank")
        String cardValue
) {}