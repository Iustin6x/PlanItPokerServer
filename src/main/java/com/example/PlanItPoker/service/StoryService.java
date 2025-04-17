package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.StoryDTO;

import java.util.List;
import java.util.UUID;

public interface StoryService {
    StoryDTO createStory(UUID roomId, String name);

    StoryDTO updateStory(UUID storyId, StoryDTO storyDTO);

    StoryDTO updateStoryName(UUID storyId, String name);

    void deleteStory(UUID storyId);

    List<StoryDTO> getStoriesByRoomId(UUID roomId);

    StoryDTO getStoryById(UUID storyId);

    StoryDTO updateStoryOrder(UUID storyId, int newOrder);

    StoryDTO updateStoryStatus(UUID storyId, StoryStatus status);

    StoryDTO getNextStoryToVote(UUID roomId);
}