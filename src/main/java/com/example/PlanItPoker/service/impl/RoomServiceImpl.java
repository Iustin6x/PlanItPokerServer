package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.exception.RoomNotFoundException;
import com.example.PlanItPoker.exception.UnauthorizedActionException;
import com.example.PlanItPoker.exception.UserNotFoundException;
import com.example.PlanItPoker.model.*;
import com.example.PlanItPoker.model.enums.CardType;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.payload.DTOs.*;
import com.example.PlanItPoker.payload.request.RoomRequest;
import com.example.PlanItPoker.repository.*;
import com.example.PlanItPoker.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final RoomSettingsRepository roomSettingsRepository;
    private final PlayerRepository playerRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final VoteSessionRepository voteSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RoomServiceImpl(RoomRepository roomRepository, RoomSettingsRepository roomSettingsRepository, PlayerRepository playerRepository, StoryRepository storyRepository, UserRepository userRepository, VoteSessionRepository voteSessionRepository, ApplicationEventPublisher eventPublisher) {
        this.roomRepository = roomRepository;
        this.roomSettingsRepository = roomSettingsRepository;
        this.playerRepository = playerRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.voteSessionRepository = voteSessionRepository;
        this.eventPublisher = eventPublisher;
    }


    @Override
    @Transactional
    public RoomListDTO createRoom(RoomRequest request, UUID creatorId) {
        try {
            User creator = userRepository.findById(creatorId)
                    .orElseThrow(() -> new UserNotFoundException(creatorId));

            Room room = request.toEntity();
            room.setInviteLink(UUID.randomUUID().toString());

            RoomSettings savedSettings = roomSettingsRepository.save(room.getRoomSettings());
            room.setRoomSettings(savedSettings);

            Room savedRoom = roomRepository.save(room);

            logger.info("Room created: " + savedRoom);

            Player moderatorPlayer = new Player();
            moderatorPlayer.setUser(creator);
            moderatorPlayer.setRole(PlayerRole.MODERATOR);
            moderatorPlayer.setIsConnected(false);

            savedRoom.addPlayer(moderatorPlayer);
            playerRepository.save(moderatorPlayer);

            Room roomWithPlayers = roomRepository.findById(savedRoom.getId())
                    .orElseThrow(() -> new RuntimeException("Room not found after creation"));

            return RoomListDTO.fromEntity(roomWithPlayers, creatorId);

        } catch (UserNotFoundException e) {
            logger.error("User not found during room creation: {}", e.getMessage());
            throw e; // Re-throw exception
        } catch (Exception e) {
            logger.error("Unexpected error during room creation: {}", e.getMessage());
            throw new RuntimeException("An error occurred while creating the room", e);
        }
    }


    @Override
    public List<RoomListDTO> getUserRooms(UUID userId) {
        try {
            List<Room> userRooms = roomRepository.findAllByUserId(userId);
            return userRooms.stream()
                    .map(room -> RoomListDTO.fromEntity(room, userId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving rooms for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("An error occurred while retrieving the user's rooms", e);
        }
    }

    @Override
    public void deleteRoom(UUID roomId) {
        try {
            Room room = getRoomById(roomId);

            List<Story> stories = storyRepository.findAllByRoom_Id(roomId);
            storyRepository.deleteAll(stories);

            List<Player> players = playerRepository.findAllByRoomId(roomId);
            playerRepository.deleteAll(players);

            roomRepository.delete(room);
            logger.info("Room {} deleted successfully", roomId);
        } catch (RoomNotFoundException e) {
            logger.error("Room not found for deletion: {}", e.getMessage());
            throw e; // Re-throw exception
        } catch (Exception e) {
            logger.error("Unexpected error during room deletion: {}", e.getMessage());
            throw new RuntimeException("An error occurred while deleting the room", e);
        }
    }

    @Override
    public Room updateRoom(UUID roomId, RoomRequest request, UUID userId) {
        try {
            Room room = getRoomById(roomId);

            Player player = playerRepository.findByUserIdAndRoomId(userId, roomId)
                    .orElseThrow(() -> new UnauthorizedActionException("User is not a participant in the room"));

            if (player.getRole() != PlayerRole.MODERATOR) {
                throw new UnauthorizedActionException("Only moderators can update the room.");
            }

            if (request.getName() != null) {
                room.setName(request.getName());
            }

            if (request.getCardType() != null) {
                room.setCardType(request.getCardType());
                if (request.getCardType() != CardType.CUSTOM) {
                    room.getCustomCards().clear();
                } else {
                    if (request.getCards() != null) {
                        room.getCustomCards().clear();
                        request.getCards().forEach(room::addCustomCard);
                    }
                }
            } else if (request.getCards() != null && room.getCardType() == CardType.CUSTOM) {
                room.getCustomCards().clear();
                request.getCards().forEach(room::addCustomCard);
            }

            if (request.getRoomSettings() != null) {
                RoomSettings settings = room.getRoomSettings();
                if (settings == null) {
                    settings = new RoomSettings();
                }
                settings.setAllowQuestionMark(request.getRoomSettings().isAllowQuestionMark());
                settings.setAllowVoteModification(request.getRoomSettings().isAllowVoteModification());

                roomSettingsRepository.save(settings);
                room.setRoomSettings(settings);
            }

            Room updatedRoom = roomRepository.save(room);
            logger.info("Room {} updated successfully", roomId);
            return updatedRoom;

        } catch (UnauthorizedActionException e) {
            logger.error("Unauthorized action during room update: {}", e.getMessage());
            throw e; // Re-throw exception
        } catch (Exception e) {
            logger.error("Unexpected error during room update: {}", e.getMessage());
            throw new RuntimeException("An error occurred while updating the room", e);
        }
    }


    @Override
    @Transactional
    public Room getRoomById(UUID roomId) {
        try {
            Room room = roomRepository.findByIdWithPlayers(roomId);
            if (room == null) {
                throw new RoomNotFoundException(roomId);
            }
            return room;
        } catch (RoomNotFoundException e) {
            logger.error("Room not found: {}", e.getMessage());
            throw e; // Re-throw exception
        } catch (Exception e) {
            logger.error("Unexpected error during room retrieval: {}", e.getMessage());
            throw new RuntimeException("An error occurred while retrieving the room", e);
        }
    }


    @Override
    public RoomResponseDTO getRoomInfo(UUID roomId) {
        try {
            Room room = getRoomById(roomId);
            return RoomResponseDTO.fromEntity(room);
        } catch (RoomNotFoundException e) {
            logger.error("Room not found with id {}: {}", roomId, e.getMessage());
            throw e; // Re-throw exception for higher-level handling
        } catch (Exception e) {
            logger.error("Unexpected error while fetching room info for roomId {}: {}", roomId, e.getMessage());
            throw new RuntimeException("An error occurred while fetching room info", e);
        }
    }

    @Override
    public RoomSettingsDTO changeRoomSettings(UUID roomId, RoomSettingsDTO settingsRequest, UUID userId) {
        try {
            Room room = getRoomById(roomId);

            Player player = playerRepository.findByUserIdAndRoomId(userId, roomId)
                    .orElseThrow(() -> new UnauthorizedActionException("User is not a participant in the room"));

            if (player.getRole() != PlayerRole.MODERATOR) {
                throw new UnauthorizedActionException("Only moderators can change the room settings.");
            }

            RoomSettings roomSettings = room.getRoomSettings();

            if (roomSettings == null) {
                roomSettings = new RoomSettings();
                room.setRoomSettings(roomSettings);
            }

            if (settingsRequest.isAllowQuestionMark() != roomSettings.isAllowQuestionMark()) {
                roomSettings.setAllowQuestionMark(settingsRequest.isAllowQuestionMark());
            }
            if (settingsRequest.isAllowVoteModification() != roomSettings.isAllowVoteModification()) {
                roomSettings.setAllowVoteModification(settingsRequest.isAllowVoteModification());
            }

            roomSettingsRepository.save(roomSettings);
            logger.info("Room settings updated successfully for room {}", roomId);

            return RoomSettingsDTO.fromEntity(roomSettings);

        } catch (UnauthorizedActionException e) {
            logger.error("Unauthorized action during room settings change: {}", e.getMessage());
            throw e; // Re-throw exception
        } catch (Exception e) {
            logger.error("Unexpected error during room settings change: {}", e.getMessage());
            throw new RuntimeException("An error occurred while changing the room settings", e);
        }
    }


}
