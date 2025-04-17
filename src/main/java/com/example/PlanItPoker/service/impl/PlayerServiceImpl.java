package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.RoomNotFoundException;
import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.model.Vote;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.payload.DTOs.PlayerDTO;
import com.example.PlanItPoker.repository.PlayerRepository;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.UserRepository;
import com.example.PlanItPoker.repository.VoteRepository;
import com.example.PlanItPoker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final VoteRepository voteRepository;

    @Override
    @Transactional
    public PlayerDTO joinRoom(UUID roomId, UUID userId) {
        // Căutăm utilizatorul pe baza userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Căutăm camera
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        // Verificăm dacă utilizatorul este deja în cameră
        Optional<Player> existingPlayer = playerRepository.findByRoomIdAndUserId(roomId, userId);

        if (existingPlayer.isPresent()) {
            // Dacă există deja, doar actualizăm statusul de conectare
            Player player = existingPlayer.get();
            player.setConnected(true);
            playerRepository.save(player);
            return PlayerDTO.fromEntity(player);
        }

        // Dacă nu există, creăm un nou player și îl adăugăm
        Player newPlayer = new Player();
        newPlayer.setRoom(room);
        newPlayer.setUser(user);
        newPlayer.setRole(PlayerRole.PLAYER); // Setăm rolul ca PLAYER
        newPlayer.setConnected(true); // Setăm ca fiind conectat

        // Salvăm noul player în baza de date
        Player savedPlayer = playerRepository.save(newPlayer);
        return PlayerDTO.fromEntity(savedPlayer); // Returnăm DTO-ul jucătorului
    }

    @Override
    @Transactional
    public PlayerDTO disconnectUserFromRoom(UUID roomId, UUID userId) {
        Optional<Player> existingPlayer = playerRepository.findByRoomIdAndUserId(roomId, userId);

        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setConnected(false);
            playerRepository.save(player);
            return PlayerDTO.fromEntity(player);
        }

        return null;
    }

    @Override
    @Transactional
    public Player findById(UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayerName(UUID playerId, String newName) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        userService.updateName(player.getUser().getId(), newName);

        return PlayerDTO.fromEntity(player);
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayerRole(UUID playerId, PlayerRole newRole) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        player.setRole(newRole);
        playerRepository.save(player);
        return PlayerDTO.fromEntity(player);
    }

    @Override
    public List<Player> getPlayersByRoomId(UUID roomId) {
        return playerRepository.findAllByRoomId(roomId);
    }

    @Transactional
    @Override
    public boolean allPlayersVoted(UUID roomId, UUID sessionId) {
        // Obținem lista de jucători online din cameră
        List<Player> onlinePlayers = playerRepository.findAllByRoomIdAndIsConnectedTrue(roomId);

        // Verificăm dacă fiecare jucător a votat în sesiunea respectivă
        for (Player player : onlinePlayers) {
            Optional<Vote> vote = voteRepository.findBySession_IdAndUser_Id(sessionId, player.getUser().getId());
            if (!vote.isPresent()) {
                return false; // Dacă un jucător nu a votat, returnează false
            }
        }

        return true; // Dacă toți jucătorii au votat, returnează true
    }
}
