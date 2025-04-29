package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.*;
import com.example.PlanItPoker.model.enums.SessionStatus;
import com.example.PlanItPoker.payload.DTOs.VoteSessionDTO;
import com.example.PlanItPoker.repository.PlayerRepository;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.StoryRepository;
import com.example.PlanItPoker.repository.VoteSessionRepository;
import com.example.PlanItPoker.service.VoteSessionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteSessionServiceImpl implements VoteSessionService {

    private static final Logger logger = LoggerFactory.getLogger(VoteSessionServiceImpl.class);

    private final VoteSessionRepository voteSessionRepository;
    private final RoomRepository roomRepository;
    private final StoryRepository storyRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    @Override
    public VoteSessionDTO createSession(UUID storyId, UUID roomId) {
        try {
            logger.info("Creating vote session for story {} in room {}", storyId, roomId);

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new UserNotFoundException(roomId));
            Story story = storyRepository.findById(storyId)
                    .orElseThrow(() -> new UserNotFoundException(storyId));

            VoteSession voteSession = new VoteSession();
            voteSession.setStartTime(LocalDateTime.now());
            voteSession.setStatus(SessionStatus.ACTIVE);
            voteSession.setRevealed(false);
            voteSession.setStory(story);
            voteSession.setRoom(room);

            voteSessionRepository.save(voteSession);
            return VoteSessionDTO.fromEntity(voteSession);
        } catch (Exception e) {
            logger.error("Error creating vote session: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public VoteSessionDTO getSession(UUID id) {
        try {
            logger.info("Fetching vote session by ID {}", id);
            VoteSession voteSession = voteSessionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            return VoteSessionDTO.fromEntity(voteSession);
        } catch (Exception e) {
            logger.error("Error fetching vote session {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public VoteSessionDTO updateSessionStatus(UUID sessionId, SessionStatus status) {
        try {
            logger.info("Updating session status for {} to {}", sessionId, status);
            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            voteSession.setStatus(status);
            voteSessionRepository.save(voteSession);
            return VoteSessionDTO.fromEntity(voteSession);
        } catch (Exception e) {
            logger.error("Error updating session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public void deleteSessionForStory(UUID storyId) {
        try {
            logger.info("Deleting vote session for story {}", storyId);
            VoteSession voteSession = voteSessionRepository.findByStoryId(storyId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            voteSessionRepository.delete(voteSession);
        } catch (Exception e) {
            logger.error("Error deleting vote session for story {}: {}", storyId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public void revealVotes(UUID sessionId) {
        try {
            logger.info("Revealing votes for session {}", sessionId);
            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

            if (voteSession.getVotes() == null || voteSession.getVotes().isEmpty()) {
                throw new IllegalStateException("Cannot reveal votes: No votes have been cast.");
            }

            voteSession.setRevealed(true);
            String result = calculateResult(voteSession);
            voteSession.setResult(result);

            voteSessionRepository.save(voteSession);
        } catch (Exception e) {
            logger.error("Error revealing votes for session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public void hideVotes(UUID sessionId) {
        try {
            logger.info("Hiding votes for session {}", sessionId);
            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));
            voteSession.setRevealed(false);
            voteSessionRepository.save(voteSession);
        } catch (Exception e) {
            logger.error("Error hiding votes for session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public VoteSessionDTO getActiveVoteSessionByRoomId(UUID roomId) {
        try {
            logger.info("Getting active vote session for room {}", roomId);
            return voteSessionRepository.findByRoom_IdAndStatus(roomId, SessionStatus.ACTIVE)
                    .map(VoteSessionDTO::fromEntity)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting active vote session for room {}: {}", roomId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public VoteSessionDTO getSessionByStoryId(UUID storyId) {
        try {
            logger.info("Getting session by story ID {}", storyId);
            VoteSession session = voteSessionRepository.findByStoryId(storyId)
                    .orElseThrow(() -> new EntityNotFoundException("Vote session not found for story"));
            session.getVotes().size(); // Trigger loading if lazy
            return VoteSessionDTO.fromEntity(session);
        } catch (Exception e) {
            logger.error("Error getting session by story ID {}: {}", storyId, e.getMessage(), e);
            throw e;
        }
    }

    private String calculateResult(VoteSession voteSession) {
        return voteSession.getVotes().stream()
                .map(Vote::getCardValue)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No Votes");
    }
}
