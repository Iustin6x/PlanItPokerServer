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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private VoteSessionRepository voteSessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerRepository playerRepository;


    @Transactional
    @Override
    public VoteDTO addVote(UUID sessionId, UUID playerId , String cardValue) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        UUID userId = player.getUser().getId();
        Optional<Vote> existingVote = voteRepository.findBySession_IdAndUser_Id(sessionId, userId);

        player.setHasVoted(true);
        playerRepository.save(player);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            vote.setCardValue(cardValue);
            voteRepository.save(vote);
            return VoteDTO.fromEntity(vote);
        }

        Vote newVote = new Vote();
        newVote.setSession(voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found with ID: " + sessionId)));
        newVote.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId)));
        newVote.setCardValue(cardValue);

        voteRepository.save(newVote);

        return VoteDTO.fromEntity(newVote);
    }


    @Override
    public List<VoteDTO> getVotesForSession(UUID sessionId) {
        List<Vote> votes = voteRepository.findBySession_Id(sessionId);

        return votes.stream()
                .map(VoteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void clearVotes(UUID sessionId) {
        voteRepository.deleteBySession_Id(sessionId);

        VoteSession voteSession = voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

        voteSession.setRevealed(false);
        voteSessionRepository.save(voteSession);
    }

    @Transactional
    @Override
    public VoteSession endVoteSession(UUID sessionId, String finalValue) {
        VoteSession voteSession = voteSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Vote session not found"));

        Story story = voteSession.getStory();
        story.setFinalResult(finalValue);
        story.setStatus(StoryStatus.COMPLETED);
        storyRepository.save(story);

        voteSession.setStatus(SessionStatus.COMPLETED);
        voteSessionRepository.save(voteSession);

        return voteSession;
    }




}
