package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.payload.DTOs.VoteDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteService {
    @Transactional
    VoteDTO addVote(UUID sessionId, UUID userId, String cardValue);

    List<VoteDTO> getVotesForSession(UUID sessionId);

    @Transactional
    void clearVotes(UUID sessionId);

    @Transactional
    VoteSession endVoteSession(UUID sessionId, String finalValue);

    @Transactional
    Optional<VoteDTO> getVoteBySessionIdAndUserId(UUID sessionId, UUID userId);
}
