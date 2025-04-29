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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final VoteRepository voteRepository;

    @Override
    @Transactional
    public PlayerDTO joinRoom(UUID roomId, UUID userId) {
        try {
            // Căutăm utilizatorul pe baza userId
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            // Căutăm camera
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException(roomId));

            // Verificăm dacă utilizatorul este deja în cameră
            Optional<Player> existingPlayer = playerRepository.findByRoomIdAndUserId(roomId, userId);

            if (existingPlayer.isPresent()) {
                Player player = existingPlayer.get();
                player.setIsConnected(true);
                playerRepository.save(player);
                return PlayerDTO.fromEntity(player);
            }

            // Dacă nu există, creăm un nou player și îl adăugăm
            Player newPlayer = new Player();
            newPlayer.setRoom(room);
            newPlayer.setUser(user);
            newPlayer.setRole(PlayerRole.PLAYER); // Setăm rolul ca PLAYER
            newPlayer.setIsConnected(true); // Setăm ca fiind conectat

            // Salvăm noul player în baza de date
            Player savedPlayer = playerRepository.save(newPlayer);
            return PlayerDTO.fromEntity(savedPlayer); // Returnăm DTO-ul jucătorului
        } catch (UserNotFoundException | RoomNotFoundException e) {
            logger.error("Error during joinRoom: {}", e.getMessage());
            throw e; // Rethrow exception pentru a fi capturată la nivelul controller-ului
        } catch (Exception e) {
            logger.error("Unexpected error during joinRoom: {}", e.getMessage());
            throw new RuntimeException("An error occurred while joining the room");
        }
    }


    @Override
    @Transactional
    public PlayerDTO disconnectUserFromRoom(UUID roomId, UUID userId) {
        try {
            Optional<Player> existingPlayer = playerRepository.findByRoomIdAndUserId(roomId, userId);

            if (existingPlayer.isPresent()) {
                Player player = existingPlayer.get();
                player.setIsConnected(false);
                playerRepository.save(player);
                return PlayerDTO.fromEntity(player);
            }

            return null;
        } catch (Exception e) {
            logger.error("Unexpected error during disconnectUserFromRoom: {}", e.getMessage());
            throw new RuntimeException("An error occurred while disconnecting the user from the room");
        }
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
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            userService.updateName(player.getUser().getId(), newName);

            return PlayerDTO.fromEntity(player);
        } catch (IllegalArgumentException e) {
            logger.error("Error during updatePlayerName: {}", e.getMessage());
            throw e; // Re-throw the exception to be caught by controller
        } catch (Exception e) {
            logger.error("Unexpected error during updatePlayerName: {}", e.getMessage());
            throw new RuntimeException("An error occurred while updating the player's name");
        }
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayerRole(UUID playerId, PlayerRole newRole) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            player.setRole(newRole);
            playerRepository.save(player);
            return PlayerDTO.fromEntity(player);
        } catch (IllegalArgumentException e) {
            logger.error("Error during updatePlayerRole: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during updatePlayerRole: {}", e.getMessage());
            throw new RuntimeException("An error occurred while updating the player's role");
        }
    }

    @Override
    public List<Player> getPlayersByRoomId(UUID roomId) {
        return playerRepository.findAllByRoomId(roomId);
    }

    @Transactional
    @Override
    public boolean allPlayersVoted(UUID roomId, UUID sessionId) {
        List<Player> onlinePlayers = playerRepository.findAllByRoomIdAndIsConnectedTrue(roomId);

        for (Player player : onlinePlayers) {
            if (player.getRole() != PlayerRole.OBSERVER) {
                Optional<Vote> vote = voteRepository.findBySession_IdAndUser_Id(sessionId, player.getUser().getId());
                if (!vote.isPresent()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void recordAction(UUID playerId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            player.setLastActionTime(LocalDateTime.now());
            playerRepository.save(player);
        } catch (IllegalArgumentException e) {
            logger.error("Error during recordAction: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during recordAction: {}", e.getMessage());
            throw new RuntimeException("An error occurred while recording the player's action");
        }
    }
}
