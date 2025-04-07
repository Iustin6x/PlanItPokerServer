package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findBySessionId(UUID sessionId);
}
