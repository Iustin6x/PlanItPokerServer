package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.VoteSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoteSessionRepository extends JpaRepository<VoteSession, UUID> {
    Optional<VoteSession> findByStoryId(UUID storyId);
}
