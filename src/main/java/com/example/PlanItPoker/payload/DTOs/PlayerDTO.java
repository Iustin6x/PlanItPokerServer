package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.enums.PlayerRole;

import java.util.UUID;

public record PlayerDTO(
        UUID id,
        UUID userId,
        String name,
        String vote,
        boolean connected,
        PlayerRole role
) {
    public static PlayerDTO fromEntity(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getUser().getId(),
                player.getUser().getName(),
                player.getVote(),
                player.isConnected(),
                player.getRole()
        );
    }
}
