package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.enums.PlayerRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomDTO(
        UUID id,
        String name,
        String lastVotedStory,
        int totalPoints,
        String inviteLink,
        PlayerRole role
) {}
