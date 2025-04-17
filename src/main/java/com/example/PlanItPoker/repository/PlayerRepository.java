package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByRoomIdAndUserId(UUID roomId, UUID userId);
    Optional<Player> findByUserId(UUID userId);
    List<Player> findByRoomId(UUID roomId);
    List<Player> findAllByRoomId(UUID roomId);
    void deleteAllByRoomId(UUID roomId);
    List<Player> findAllByRoomIdAndIsConnectedTrue(UUID roomId);

}
