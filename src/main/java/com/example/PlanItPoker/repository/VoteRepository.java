package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    List<Vote> findBySession_Id(UUID sessionId);
    void deleteBySession_Id(UUID sessionId);
    Optional<Vote> findBySession_IdAndUser_Id(UUID sessionId, UUID userId);
}
