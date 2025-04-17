package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.SessionStatus;
import com.example.PlanItPoker.payload.DTOs.VoteSessionDTO;
import com.example.PlanItPoker.repository.PlayerRepository;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.StoryRepository;
import com.example.PlanItPoker.repository.VoteSessionRepository;
import com.example.PlanItPoker.service.VoteSessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteSessionServiceImpl implements VoteSessionService {
    private final VoteSessionRepository voteSessionRepository;
    private final RoomRepository roomRepository;
    private final StoryRepository storyRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    @Override
    public VoteSessionDTO createSession(UUID storyId, UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new UserNotFoundException(roomId));
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new UserNotFoundException(storyId));
        VoteSession voteSession = new VoteSession();
        voteSession.setStartTime(LocalDateTime.now());
        voteSession.setStatus(SessionStatus.ACTIVE);
        voteSession.setRevealed(false);
        voteSession.setStory(story);
        voteSession.setRoom(room); // Adăugăm camera

        voteSessionRepository.save(voteSession);

        return VoteSessionDTO.fromEntity(voteSession);
    }

    @Override
    public VoteSessionDTO getSession(UUID id) {
        VoteSession voteSession = voteSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        return VoteSessionDTO.fromEntity(voteSession);
    }

    @Transactional
    @Override
    public VoteSessionDTO updateSessionStatus(UUID sessionId, SessionStatus status) {
        VoteSession voteSession = voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        voteSession.setStatus(status);
        voteSessionRepository.save(voteSession);

        return VoteSessionDTO.fromEntity(voteSession);
    }

    @Transactional
    @Override
    public void deleteSessionForStory(UUID storyId) {
        VoteSession voteSession = voteSessionRepository.findByStoryId(storyId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        voteSessionRepository.delete(voteSession);

    }

    @Transactional
    @Override
    public void revealVotes(UUID sessionId) {
        VoteSession voteSession = voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

        voteSession.setRevealed(true);
        voteSessionRepository.save(voteSession);

    }

    @Transactional
    @Override
    public void hideVotes(UUID sessionId) {
        VoteSession voteSession = voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

        voteSession.setRevealed(false);
        voteSessionRepository.save(voteSession);

    }

    @Transactional
    @Override
    public VoteSessionDTO getActiveVoteSessionByRoomId(UUID roomId) {
        return voteSessionRepository.findByRoom_IdAndStatus(roomId, SessionStatus.ACTIVE)
                .map(VoteSessionDTO::fromEntity)
                .orElse(null);
    }


}
