package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.StoryDTO;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.StoryRepository;
import com.example.PlanItPoker.service.StoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final RoomRepository roomRepository;

    @Override
    public StoryDTO createStory(UUID roomId, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        Story story = new Story();
        story.setName(name);
        story.setStatus(StoryStatus.ACTIVE); // sau orice status implicit ai nevoie
        story.setFinalResult(null); // null inițial
        story.setRoom(room);


        return StoryDTO.fromEntity(storyRepository.save(story));
    }


    @Override
    public StoryDTO updateStory(UUID storyId, StoryDTO storyDTO) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story not found"));
        if (storyDTO.name() == null || storyDTO.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }
        story.setName(storyDTO.name());
        story.setFinalResult(storyDTO.finalResult());
        story.setStatus(storyDTO.status());

        return StoryDTO.fromEntity(storyRepository.save(story));
    }

    @Override
    public StoryDTO updateStoryName(UUID storyId, String name) {
        // Găsește story-ul după ID
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story not found"));

        // Verifică dacă numele nu este null sau gol
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Story name cannot be null or empty");
        }

        // Setează doar noul nume
        story.setName(name);

        // Salvează story-ul actualizat în baza de date
        return StoryDTO.fromEntity(storyRepository.save(story));
    }

    @Override
    public void deleteStory(UUID storyId) {
        storyRepository.deleteById(storyId);
    }

    @Override
    public List<StoryDTO> getStoriesByRoomId(UUID roomId) {
        return storyRepository.findAllByRoom_Id(roomId)
                .stream()
                .map(StoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public StoryDTO getStoryById(UUID storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("Story not found"));
        return StoryDTO.fromEntity(story);
    }


    @Override
    public StoryDTO updateStoryStatus(UUID storyId, StoryStatus status) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));
        story.setStatus(status);
        storyRepository.save(story);
        return StoryDTO.fromEntity(story);
    }

    @Override
    public StoryDTO getNextStoryToVote(UUID roomId) {
        Story story = storyRepository.findFirstByRoom_IdAndStatus(roomId, StoryStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Story not found"));
        return StoryDTO.fromEntity(story);
    }
}
