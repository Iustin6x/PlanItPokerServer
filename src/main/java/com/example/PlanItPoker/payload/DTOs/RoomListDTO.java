package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.model.enums.StoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomListDTO(
        UUID id,
        String name,
        String lastVotedStory,
        int totalPoints,
        String inviteLink,
        PlayerRole role,
        LocalDateTime lastActionTime
) {
    public static RoomListDTO fromEntity(Room room, UUID userId) {
        Player player = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not a participant in room"));

        LocalDateTime lastActionTime = player.getLastActionTime();

        return new RoomListDTO(
                room.getId(),
                room.getName(),
                room.getStories().stream()
                        .filter(s -> s.getStatus() == StoryStatus.COMPLETED)
                        .findFirst()
                        .map(Story::getName)
                        .orElse("No completed stories"),
                calculateTotalPoints(room),
                room.getInviteLink(),
                player.getRole(),
                lastActionTime
        );
    }

    private static int calculateTotalPoints(Room room) {
        return room.getStories().stream()
                .filter(s -> s.getStatus() == StoryStatus.COMPLETED)
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s.getFinalResult());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();
    }
}