package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record VoteSessionDTO(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SessionStatus status,
        boolean revealed,
        UUID storyId,
        UUID roomId,
        List<VoteDTO> votes  // Adăugăm lista de voturi
) {
    public static VoteSessionDTO fromEntity(VoteSession session) {
        List<VoteDTO> votes = session.getVotes().stream()
                .map(VoteDTO::fromEntity)  // Transforma voturile în DTO-uri
                .collect(Collectors.toList());

        return new VoteSessionDTO(
                session.getId(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus(),
                session.isRevealed(),
                session.getStory().getId(),
                session.getRoom().getId(),
                votes
        );
    }
}
