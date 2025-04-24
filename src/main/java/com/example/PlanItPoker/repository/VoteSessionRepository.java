package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VoteSessionRepository extends JpaRepository<VoteSession, UUID> {
    Optional<VoteSession> findByStoryId(UUID storyId);
    Optional<VoteSession> findByRoom_IdAndStatus(UUID roomId, SessionStatus status);
    @Query("SELECT vs FROM VoteSession vs LEFT JOIN FETCH vs.votes WHERE vs.id = :sessionId")
    Optional<VoteSession> findByIdWithVotes(UUID sessionId);

}
