package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.RoomNotFoundException;
import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.model.enums.CardType;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.RoomDTO;
import com.example.PlanItPoker.payload.request.RoomRequest;
import com.example.PlanItPoker.repository.PlayerRepository;
import com.example.PlanItPoker.repository.RoomRepository;
import com.example.PlanItPoker.repository.StoryRepository;
import com.example.PlanItPoker.repository.UserRepository;
import com.example.PlanItPoker.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    public RoomServiceImpl(RoomRepository roomRepository, PlayerRepository playerRepository, StoryRepository storyRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RoomDTO createRoom(RoomRequest request, UUID creatorId) {
        Room room = new Room();
        room.setName(request.name());
        room.setCardType(request.cardType());

        String inviteLink = UUID.randomUUID().toString();
        room.setInviteLink(inviteLink);

        if(request.cards() != null) {
            request.cards().forEach(room::addCustomCard);
        }

        Room savedRoom = roomRepository.save(room);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException(creatorId));

        Player moderatorPlayer = new Player();
        moderatorPlayer.setRoom(savedRoom);
        moderatorPlayer.setUser(creator);
        moderatorPlayer.setRole(PlayerRole.MODERATOR);
        moderatorPlayer.setConnected(true);

        savedRoom.getPlayers().add(moderatorPlayer);

        playerRepository.save(moderatorPlayer);


        return convertToDTO(savedRoom, creatorId);
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
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

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
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

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

    private RoomDTO convertToDTO(Room room, UUID userId) {
        PlayerRole role = room.getPlayers().stream()
                .filter(player -> player.getUser().getId().equals(userId))
                .findFirst()
                .map(Player::getRole)
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
                role
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


}
