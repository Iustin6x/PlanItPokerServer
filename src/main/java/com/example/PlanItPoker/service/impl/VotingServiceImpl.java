package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Vote;
import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.repository.PlayerRepository;
import com.example.PlanItPoker.repository.VoteRepository;
import com.example.PlanItPoker.repository.VoteSessionRepository;
import com.example.PlanItPoker.service.VotingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VotingServiceImpl implements VotingService {
    private final VoteSessionRepository voteSessionRepository;
    private final PlayerRepository playerRepository;
    private final VoteRepository voteRepository;

    public VotingServiceImpl(VoteSessionRepository voteSessionRepository, PlayerRepository playerRepository, VoteRepository voteRepository) {
        this.voteSessionRepository = voteSessionRepository;
        this.playerRepository = playerRepository;
        this.voteRepository = voteRepository;
    }


    public void submitVote(UUID sessionId, UUID userId, String value) {
        VoteSession session = voteSessionRepository.findById(sessionId).orElseThrow();
        Player player = playerRepository.findByRoomIdAndUserId(session.getStory().getRoomId(), userId)
                .orElseThrow();

        if(session.isRevealed()) {
            throw new IllegalStateException("Voting already revealed");
        }

        Vote vote = new Vote();
        vote.setSession(session);
        vote.setUser(player.getUser());
        vote.setCardValue(value);
        voteRepository.save(vote);

        player.setHasVoted(true);
        playerRepository.save(player);
    }

    public Map<String, Long> revealVotes(UUID sessionId) {
        VoteSession session = voteSessionRepository.findById(sessionId).orElseThrow();
        session.setRevealed(true);

        return voteRepository.findBySessionId(sessionId).stream()
                .collect(Collectors.groupingBy(Vote::getCardValue, Collectors.counting()));
    }
}
