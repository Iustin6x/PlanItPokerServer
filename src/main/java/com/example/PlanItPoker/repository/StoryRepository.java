package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.StoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {
    Optional<Story> findFirstByRoom_IdAndStatus(UUID roomId, StoryStatus status);
    List<Story> findAllByRoom_Id(UUID roomId);
    void deleteAllByRoom_Id(UUID roomId);

}