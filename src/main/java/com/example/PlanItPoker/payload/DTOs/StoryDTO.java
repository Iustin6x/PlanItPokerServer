package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.StoryStatus;

import java.util.UUID;

public record StoryDTO(
        UUID id,
        String name,
        String finalResult,
        StoryStatus status
) {
    public static StoryDTO fromEntity(Story story) {
        return new StoryDTO(
                story.getId(),
                story.getName(),
                story.getFinalResult(),
                story.getStatus()
        );
    }
}
