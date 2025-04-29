package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.RoomNotFoundException;
import com.example.PlanItPoker.exception.StoryNotFoundException;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.StoryDTO;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.StoryRepository;
import com.example.PlanItPoker.service.StoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    private final StoryRepository storyRepository;
    private final RoomRepository roomRepository;

    @Override
    public StoryDTO createStory(UUID roomId, String name) {
        logger.info("Creating story in room: {}", roomId);

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempted to create story with empty name");
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    logger.error("Room not found with ID: {}", roomId);
                    return new RoomNotFoundException(roomId);
                });

        Story story = new Story();
        story.setName(name);
        story.setStatus(StoryStatus.ACTIVE);
        story.setFinalResult(null);
        story.setRoom(room);

        Story saved = storyRepository.save(story);
        logger.info("Story created with ID: {}", saved.getId());

        return StoryDTO.fromEntity(saved);
    }

    @Override
    public StoryDTO updateStory(UUID storyId, StoryDTO storyDTO) {
        logger.info("Updating story with ID: {}", storyId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> {
                    logger.error("Story not found with ID: {}", storyId);
                    return new StoryNotFoundException(storyId);
                });

        if (storyDTO.name() == null || storyDTO.name().trim().isEmpty()) {
            logger.warn("Attempted to update story with empty name");
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }

        story.setName(storyDTO.name());
        story.setFinalResult(storyDTO.finalResult());
        story.setStatus(storyDTO.status());

        logger.info("Story with ID: {} updated successfully", storyId);
        return StoryDTO.fromEntity(storyRepository.save(story));
    }

    @Override
    public StoryDTO updateStoryName(UUID storyId, String name) {
        logger.info("Updating name for story ID: {}", storyId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> {
                    logger.error("Story not found with ID: {}", storyId);
                    return new StoryNotFoundException(storyId);
                });

        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempted to set empty name for story ID: {}", storyId);
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }

        story.setName(name);
        logger.info("Story name updated for ID: {}", storyId);
        return StoryDTO.fromEntity(storyRepository.save(story));
    }

    @Override
    public void deleteStory(UUID storyId) {
        logger.info("Deleting story with ID: {}", storyId);

        if (!storyRepository.existsById(storyId)) {
            logger.error("Cannot delete; story not found with ID: {}", storyId);
            throw new StoryNotFoundException(storyId);
        }

        storyRepository.deleteById(storyId);
        logger.info("Story deleted with ID: {}", storyId);
    }

    @Override
    public List<StoryDTO> getStoriesByRoomId(UUID roomId) {
        logger.info("Fetching stories for room ID: {}", roomId);

        if (!roomRepository.existsById(roomId)) {
            logger.error("Room not found with ID: {}", roomId);
            throw new RoomNotFoundException(roomId);
        }

        return storyRepository.findAllByRoom_Id(roomId)
                .stream()
                .map(StoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public StoryDTO getStoryById(UUID storyId) {
        logger.info("Fetching story by ID: {}", storyId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> {
                    logger.error("Story not found with ID: {}", storyId);
                    return new StoryNotFoundException(storyId);
                });

        return StoryDTO.fromEntity(story);
    }

    @Override
    public StoryDTO updateStoryStatus(UUID storyId, StoryStatus status) {
        logger.info("Updating status for story ID: {} to {}", storyId, status);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> {
                    logger.error("Story not found with ID: {}", storyId);
                    return new StoryNotFoundException(storyId);
                });

        story.setStatus(status);
        return StoryDTO.fromEntity(storyRepository.save(story));
    }

    @Override
    public StoryDTO getNextStoryToVote(UUID roomId) {
        logger.info("Fetching next story to vote in room ID: {}", roomId);

        Story story = storyRepository.findFirstByRoom_IdAndStatus(roomId, StoryStatus.ACTIVE)
                .orElseThrow(() -> {
                    logger.warn("No active stories found in room ID: {}", roomId);
                    return new StoryNotFoundException(roomId);
                });

        return StoryDTO.fromEntity(story);
    }
}
