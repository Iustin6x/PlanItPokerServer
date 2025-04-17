package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.SessionStatus;
import com.example.PlanItPoker.payload.DTOs.VoteSessionDTO;
import jakarta.transaction.Transactional;

import java.util.UUID;

public interface VoteSessionService {

    @Transactional
    VoteSessionDTO createSession(UUID storyId, UUID roomId);

    VoteSessionDTO getSession(UUID id);

    @Transactional
    VoteSessionDTO updateSessionStatus(UUID sessionId, SessionStatus status);

    @Transactional
    void deleteSessionForStory(UUID storyId);

    @Transactional
    void revealVotes(UUID sessionId);

    @Transactional
    void hideVotes(UUID sessionId);

    VoteSessionDTO getActiveVoteSessionByRoomId(UUID roomId);


}
