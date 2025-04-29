package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.*;
import com.example.PlanItPoker.model.enums.SessionStatus;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.VoteDTO;
import com.example.PlanItPoker.repository.*;
import com.example.PlanItPoker.service.PlayerService;
import com.example.PlanItPoker.service.VoteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private static final Logger logger = LoggerFactory.getLogger(VoteServiceImpl.class);

    private final VoteRepository voteRepository;
    private final VoteSessionRepository voteSessionRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    @Transactional
    @Override
    public VoteDTO addVote(UUID sessionId, UUID playerId, String cardValue) {
        try {
            logger.info("Adding vote for session {}, player {}, cardValue {}", sessionId, playerId, cardValue);

            // Găsește jucătorul
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            UUID userId = player.getUser().getId();

            // Găsește sesiunea de vot
            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId));

            // Verifică setările camerei
            RoomSettings roomSettings = voteSession.getRoom().getRoomSettings();

            // Permite votarea doar dacă sesiunea nu a fost deja dezvăluită și modificările de vot sunt permise
            if (voteSession.isRevealed()) {
                throw new IllegalStateException("Cannot vote again, session already revealed.");
            }

            // Dacă modificările de vot nu sunt permise, nu permite votarea sau modificarea votului
            if (roomSettings != null && !roomSettings.isAllowVoteModification()) {
                Optional<Vote> existingVote = voteRepository.findBySession_IdAndUser_Id(sessionId, userId);
                if (existingVote.isPresent()) {
                    throw new IllegalStateException("Vote modification is not allowed in this room.");
                }
            }

            // Verifică dacă există deja un vot al jucătorului pentru sesiune
            Optional<Vote> existingVote = voteRepository.findBySession_IdAndUser_Id(sessionId, userId);
            if (existingVote.isPresent()) {
                // Dacă există un vot, îl actualizează
                Vote vote = existingVote.get();
                vote.setCardValue(cardValue);
                return VoteDTO.fromEntity(voteRepository.save(vote));
            }

            // Dacă nu există vot, creează unul nou
            Vote newVote = new Vote();
            newVote.setSession(voteSession);
            newVote.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId)));
            newVote.setCardValue(cardValue);

            return VoteDTO.fromEntity(voteRepository.save(newVote), false);

        } catch (Exception e) {
            logger.error("Error adding vote for session {} and player {}: {}", sessionId, playerId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<VoteDTO> getVotesForSession(UUID sessionId) {
        try {
            logger.info("Getting votes for session {}", sessionId);
            return voteRepository.findBySession_Id(sessionId)
                    .stream()
                    .map(VoteDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching votes for session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public Optional<VoteDTO> getVoteBySessionIdAndUserId(UUID sessionId, UUID userId) {
        try {
            logger.info("Fetching vote for session {} and user {}", sessionId, userId);
            return voteRepository.findBySession_IdAndUser_Id(sessionId, userId)
                    .map(vote -> VoteDTO.fromEntity(vote, true));
        } catch (Exception e) {
            logger.error("Error fetching vote for session {} and user {}: {}", sessionId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public void clearVotes(UUID sessionId) {
        try {
            logger.info("Clearing votes for session {}", sessionId);
            voteRepository.deleteBySession_Id(sessionId);

            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

            voteSession.setRevealed(false);
            voteSessionRepository.save(voteSession);
            logger.info("Votes cleared and session marked unrevealed for {}", sessionId);
        } catch (Exception e) {
            logger.error("Error clearing votes for session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public VoteSession endVoteSession(UUID sessionId, String finalValue) {
        try {
            logger.info("Ending vote session {} with final value {}", sessionId, finalValue);

            VoteSession voteSession = voteSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

            Story story = voteSession.getStory();
            story.setFinalResult(finalValue);
            story.setStatus(StoryStatus.COMPLETED);
            storyRepository.save(story);

            voteSession.setStatus(SessionStatus.COMPLETED);
            voteSessionRepository.save(voteSession);

            logger.info("Vote session {} and story {} marked as COMPLETED", sessionId, story.getId());
            return voteSession;
        } catch (Exception e) {
            logger.error("Error ending vote session {}: {}", sessionId, e.getMessage(), e);
            throw e;
        }
    }
}
