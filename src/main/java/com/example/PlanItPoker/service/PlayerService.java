package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.payload.DTOs.PlayerDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    @Transactional
    PlayerDTO joinRoom(UUID roomId, UUID userId);

    PlayerDTO disconnectUserFromRoom(UUID roomId, UUID userId);

    Player findById(UUID playerId);

    PlayerDTO updatePlayerName(UUID playerId, String newName);

    PlayerDTO updatePlayerRole(UUID playerId, PlayerRole newRole);

    List<Player> getPlayersByRoomId(UUID roomId);

    @org.springframework.transaction.annotation.Transactional
    boolean allPlayersVoted(UUID roomId, UUID sessionId);

    void recordAction(UUID playerId);
}
