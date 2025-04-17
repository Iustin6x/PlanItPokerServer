package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Vote;

import java.util.UUID;

public record VoteDTO(
        UUID id,
        UUID userId,
        UUID sessionId,
        String cardValue
) {
    public static VoteDTO fromEntity(Vote vote) {
        return new VoteDTO(
                vote.getId(),
                vote.getUser().getId(),
                vote.getSession().getId(),
                vote.getCardValue()
        );
    }
}
