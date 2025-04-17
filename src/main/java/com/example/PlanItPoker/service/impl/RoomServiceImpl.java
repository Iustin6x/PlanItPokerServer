package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.configuration.RoomUpdateEvent;
import com.example.PlanItPoker.controller.RoomController;
import com.example.PlanItPoker.exception.RoomNotFoundException;
import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.*;
import com.example.PlanItPoker.model.enums.CardType;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.model.enums.SessionStatus;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.*;
import com.example.PlanItPoker.payload.request.RoomRequest;
import com.example.PlanItPoker.repository.*;
import com.example.PlanItPoker.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.smartcardio.Card;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final VoteSessionRepository voteSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoomServiceImpl(RoomRepository roomRepository, PlayerRepository playerRepository, StoryRepository storyRepository, UserRepository userRepository, VoteSessionRepository voteSessionRepository, ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.voteSessionRepository = voteSessionRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Transactional
    public RoomDTO createRoom(RoomRequest request, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException(creatorId));

        Room room = new Room();
        room.setName(request.name());
        room.setCardType(request.cardType());
        room.setInviteLink(UUID.randomUUID().toString());

        if(request.cards() != null) {
            request.cards().forEach(room::addCustomCard);
        }

        Room savedRoom = roomRepository.save(room);

        logger.info("Room created: " + savedRoom);

        logger.info("Room created{)",creator);
        Player moderatorPlayer = new Player();
//        moderatorPlayer.setRoom(savedRoom);
        moderatorPlayer.setUser(creator);
        moderatorPlayer.setRole(PlayerRole.MODERATOR);
        moderatorPlayer.setConnected(false);

//        playerRepository.save(moderatorPlayer);

        savedRoom.addPlayer(moderatorPlayer);

        playerRepository.save(moderatorPlayer);


        Room roomWithPlayers = roomRepository.findById(savedRoom.getId())
                .orElseThrow(() -> new RuntimeException("Room not found after creation"));

        return convertToDTO(roomWithPlayers, creatorId);
    }

    @Override
    public List<RoomDTO> getUserRooms(UUID userId) {
        List<Room> userRooms = roomRepository.findAllByUserId(userId);
        return userRooms.stream()
                .map(room -> convertToDTO(room, userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRoom(UUID roomId) {
        Room room = getRoomById(roomId);

        // Delete all stories associated with the room
        List<Story> stories = storyRepository.findAllByRoom_Id(roomId);
        storyRepository.deleteAll(stories);

        // Delete all players associated with the room
        List<Player> players = playerRepository.findAllByRoomId(roomId);
        playerRepository.deleteAll(players);

        // Delete the room
        roomRepository.delete(room);
    }

    @Override
    public RoomDTO updateRoom(UUID roomId, RoomRequest request, UUID userId) {
        Room room = getRoomById(roomId);

        // Update room name if provided
        if (request.name() != null) {
            room.setName(request.name());
        }

        // Handle card type and custom cards
        if (request.cardType() != null) {
            CardType newCardType = request.cardType();
            room.setCardType(newCardType);

            if (newCardType != CardType.CUSTOM) {
                room.getCustomCards().clear();
            } else {
                // Update custom cards if provided
                if (request.cards() != null) {
                    room.getCustomCards().clear();
                    request.cards().forEach(room::addCustomCard);
                }
            }
        } else if (request.cards() != null) {
            // Update custom cards if current type is CUSTOM
            if (room.getCardType() == CardType.CUSTOM) {
                room.getCustomCards().clear();
                request.cards().forEach(room::addCustomCard);
            }
        }

        Room updatedRoom = roomRepository.save(room);
        return convertToDTO(updatedRoom, userId);
    }

    @Override
    @Transactional
    public Room getRoomById(UUID roomId) {
        Room room = roomRepository.findByIdWithPlayers(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        return room;
    }

    @Override
    @Transactional
    public RoomDetailsDTO getRoomDetails(UUID roomId) {
        Room room = getRoomById(roomId);
        room.getCustomCards().size();
        String name = room.getName();

//        List<Card> cards = room.getCustomCards();
        List<PlayerDTO> players = playerRepository.findByRoomId(roomId)
                .stream()
                .map(PlayerDTO::fromEntity)
                .toList();

        List<StoryDTO> stories = room.getStories()
                .stream()
                .map(StoryDTO::fromEntity)
                .toList();

        List<String> customCards = room.getCustomCards().stream()
                .map(x -> x.getValue())
                .toList();

        CardType cardType = room.getCardType();

        Story inProgressStory = room.getStories().stream()
                .filter(s -> s.getStatus() == StoryStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        StoryDTO currentStoryDTO = inProgressStory != null ? StoryDTO.fromEntity(inProgressStory) : null;

        VoteSession activeVoteSession = voteSessionRepository.findByRoom_IdAndStatus(roomId, SessionStatus.ACTIVE)
                .orElse(null);

        VoteSessionDTO voteSessionDTO = activeVoteSession != null ? VoteSessionDTO.fromEntity(activeVoteSession) : null;


        return new RoomDetailsDTO(name, cardType, customCards, players, stories, currentStoryDTO, voteSessionDTO);
    }

    private RoomDTO convertToDTO(Room room, UUID userId) {
        Player player = room.getPlayers().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not a participant in room"));

        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getStories().stream()
                        .filter(s -> s.getStatus() == StoryStatus.COMPLETED)
                        .findFirst()
                        .map(Story::getName)
                        .orElse("No completed stories"),
                calculateTotalPoints(room),
                room.getInviteLink(),
                player.getRole()
        );
    }

    private int calculateTotalPoints(Room room) {
        return room.getStories().stream()
                .filter(s -> s.getStatus() == StoryStatus.COMPLETED)
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s.getFinalResult());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();
    }


    @Override
    public RoomInfoDTO getRoomInfo(UUID roomId) {
        Room room = getRoomById(roomId);

        List<String> customCards = room.getCustomCards()
                .stream()
                .map(card -> card.getValue())
                .toList();

        return RoomInfoDTO.fromEntity(room);
    }

}
