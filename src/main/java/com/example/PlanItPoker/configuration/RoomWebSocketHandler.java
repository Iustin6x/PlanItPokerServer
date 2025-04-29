package com.example.PlanItPoker.configuration;

import com.example.PlanItPoker.exception.UnauthorizedActionException;
import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.RoomSettings;
import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.*;
import com.example.PlanItPoker.service.*;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RoomWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RoomWebSocketHandler.class);

    private final PlayerService playerService;
    private final UserServiceImpl userService;
    private final RoomService roomService;
    private final ObjectMapper objectMapper;
    private final StoryService storyService;
    private final VoteSessionService voteSessionService;
    private final VoteService voteService;

    private final Map<WebSocketSession, UUID> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<WebSocketSession>> roomSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        System.out.println("User connected with id: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = payload.get("type");

            switch (type) {
                case "join":
                    handleJoin(session, payload);
                    break;
                case "updateRoomSettings":
                    handleUpdateRoomSettings(session, payload);
                    break;
                case "changeRole":
                    handleChangeRole(session, payload);
                    break;
                case "changeName":
                    handleChangeName(session, payload);
                    break;
                case "createStory":
                    handleCreateStory(session, payload);
                    break;
                case "updateStory":
                    handleUpdateStory(session, payload);
                    break;
                case "deleteStory":
                    handleDeleteStory(session, payload);
                    break;
                case "getStories":
                    handleGetStories(session, payload);
                    break;
                case "startVote":
                    handleStartVote(session, payload);
                    break;
                case "skipVote":
                    handleSkipStory(session, payload);
                    break;
                case "addVote":
                    handleAddVote(session, payload);
                    break;
                case "revealVotes":
                    handleRevealVotes(session, payload);
                    break;
                case "clearVotes":
                    handleClearVotes(session, payload);
                    break;
                case "endVoteSession":
                    handleEndVoteSession(session, payload);
                    break;
                case "getStoryWithSession":
                    handleGetStoryWithSession(session, payload);
                    break;
                default:
                    sendErrorMessage(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            logger.error("Error processing message: ", e);
            sendErrorMessage(session, "An error occurred while processing your request.", e);
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, String> payload) {
        try {
            logger.info("Received join message");

            UUID roomId = UUID.fromString(payload.get("roomId"));
            String userIdStr = (String) session.getAttributes().get("userId");
            UUID userId = UUID.fromString(userIdStr);

            PlayerDTO player = playerService.joinRoom(roomId, userId);
            UUID playerId = player.id();
            session.getAttributes().put("playerId", playerId.toString());

            RoomResponseDTO roomInfo = roomService.getRoomInfo(roomId);
            List<PlayerDTO> players = playerService.getPlayersByRoomId(roomId)
                    .stream()
                    .map(PlayerDTO::fromEntity)
                    .toList();

            List<StoryDTO> stories = storyService.getStoriesByRoomId(roomId);
            VoteSessionDTO voteSession = voteSessionService.getActiveVoteSessionByRoomId(roomId);

            session.getAttributes().put("roomId", roomId.toString());
            sessionUserMap.put(session, userId);
            roomSessions.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "roomInfo",
                    "room", roomInfo
            ))));

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "playerList",
                    "players", players
            ))));

            if (stories != null && !stories.isEmpty()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "storyList",
                        "stories", stories
                ))));
            } else {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "noStories"
                ))));
            }

            if (voteSession != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("type", "voteSession");
                message.put("session", voteSession);

                voteService
                        .getVoteBySessionIdAndUserId(voteSession.id(), userId)
                        .map(VoteDTO::cardValue)
                        .ifPresent(cardValue -> message.put("hasVoted", cardValue));

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } else {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                        "type", "noActiveVoteSession"
                ))));
            }

            broadcastToRoom(roomId, Map.of(
                    "type", "playerJoined",
                    "player", player
            ));
        } catch (Exception e) {
            logger.error("Error in handleJoin: ", e);
            sendErrorMessage(session, "An error occurred while joining the room.", e);
        }
    }


    private void handleChangeName(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

            UUID targetPlayerId = UUID.fromString(payload.get("playerId"));
            String newName = payload.get("newName");

            var senderPlayer = playerService.findById(senderPlayerId);
            var targetPlayer = playerService.findById(targetPlayerId);

            if (!senderPlayerId.equals(targetPlayerId) && senderPlayer.getRole() != PlayerRole.MODERATOR) {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Not allowed to change another player's name\"}"));
                return;
            }

            playerService.updatePlayerName(targetPlayerId, newName);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "playerNameChanged",
                    "playerId", targetPlayerId,
                    "newName", newName
            ));
        } catch (Exception e) {
            logger.error("Error in handleChangeName: ", e);
            sendErrorMessage(session, "An error occurred while changing the name.", e);
        }
    }

    private void handleChangeRole(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

            UUID targetPlayerId = UUID.fromString(payload.get("playerId"));
            String newRoleString = payload.get("newRole");
            PlayerRole newRole = PlayerRole.valueOf(newRoleString.toUpperCase());

            var senderPlayer = playerService.findById(senderPlayerId);

            if (senderPlayer.getRole() != PlayerRole.MODERATOR) {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Only moderators can change roles\"}"));
                return;
            }

            playerService.updatePlayerRole(targetPlayerId, newRole);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "playerRoleChanged",
                    "playerId", targetPlayerId,
                    "newRole", newRole.toString()
            ));
        } catch (Exception e) {
            logger.error("Error in handleChangeRole: ", e);
            sendErrorMessage(session, "An error occurred while changing the role.", e);
        }
    }

    private void handleCreateStory(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));

            StoryDTO createdStory = storyService.createStory(roomId, payload.get("name"));
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "storyCreated",
                    "story", createdStory
            ));
        } catch (Exception e) {
            logger.error("Error creating story: ", e);
            sendErrorMessage(session, "An error occurred while creating the story.", e);
        }
    }

    private void handleGetStoryWithSession(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID storyId = UUID.fromString(payload.get("storyId"));

            StoryDTO story = storyService.getStoryById(storyId);
            VoteSessionDTO voteSession = voteSessionService.getSessionByStoryId(storyId);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "storyWithSession");
            response.put("story", story);
            response.put("session", voteSession != null ? voteSession : null);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error in handleGetStoryWithSession: ", e);
            sendErrorMessage(session, "An error occurred while retrieving story with session.", e);
        }
    }

    private void handleGetStories(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

            List<StoryDTO> stories = storyService.getStoriesByRoomId(roomId);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "storyList",
                    "stories", stories
            ))));
        } catch (Exception e) {
            logger.error("Error in handleGetStories: ", e);
            sendErrorMessage(session, "An error occurred while retrieving the stories.", e);
        }
    }

    private void handleUpdateStory(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID storyId = UUID.fromString(payload.get("storyId"));

            String storyName = payload.get("name");
            if (storyName == null || storyName.isEmpty()) {
                throw new IllegalArgumentException("Story name cannot be null or empty.");
            }

            StoryDTO updatedStory = storyService.updateStoryName(storyId, storyName);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "storyUpdated",
                    "story", updatedStory
            ));
        } catch (Exception e) {
            logger.error("Error in handleUpdateStory: ", e);
            sendErrorMessage(session, "An error occurred while updating the story.", e);
        }
    }


    private void handleDeleteStory(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID storyId = UUID.fromString(payload.get("storyId"));

            storyService.deleteStory(storyId);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "storyDeleted",
                    "storyId", storyId.toString()
            ));
        } catch (Exception e) {
            logger.error("Error in handleDeleteStory: ", e);
            sendErrorMessage(session, "An error occurred while deleting the story.", e);
        }
    }
    private void handleSkipStory(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID storyId = UUID.fromString(payload.get("storyId"));

            StoryDTO skippedStory = storyService.updateStoryStatus(storyId, StoryStatus.SKIPPED);
            voteSessionService.deleteSessionForStory(storyId);
            StoryDTO nextStory = storyService.getNextStoryToVote(roomId);
            playerService.recordAction(senderPlayerId);

            if (nextStory != null) {
                VoteSessionDTO voteSession = voteSessionService.createSession(nextStory.id(), roomId);
                broadcastToRoom(roomId, Map.of(
                        "type", "storySkipped",
                        "story", skippedStory,
                        "session", voteSession
                ));
            } else {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"No more stories to skip\"}"));
            }
        } catch (Exception e) {
            logger.error("Error in handleSkipStory: ", e);
            sendErrorMessage(session, "An error occurred while skipping the story.", e);
        }
    }



    private void handleStartVote(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));

            StoryDTO nextStory = storyService.getNextStoryToVote(roomId);
            if (nextStory == null) {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"No available story to vote\"}"));
                return;
            }

            VoteSessionDTO voteSession = voteSessionService.createSession(nextStory.id(), roomId);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "voteStarted",
                    "session", voteSession
            ));
        } catch (Exception e) {
            logger.error("Error in handleStartVote: ", e);
            sendErrorMessage(session, "An error occurred while starting the vote.", e);
        }
    }

    private void handleAddVote(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID playerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID sessionId = UUID.fromString(payload.get("sessionId"));
            String cardValue = payload.get("cardValue");

            var voteDTO = voteService.addVote(sessionId, playerId, cardValue);

            broadcastToRoomExceptSession(roomId, session, Map.of(
                    "type", "voteAdded",
                    "vote", voteDTO
            ));

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "voteAdded",
                    "vote", voteDTO,
                    "cardValue", cardValue
            ))));


            if (playerService.allPlayersVoted(roomId, sessionId)) {
                voteSessionService.revealVotes(sessionId);
                var voteList = voteService.getVotesForSession(sessionId);
                String calculatedResult = calculateVoteResult(voteList);

                broadcastToRoom(roomId, Map.of(
                        "type", "votesRevealed",
                        "votes", voteList,
                        "result", calculatedResult
                ));
            }
        } catch (Exception e) {
            logger.error("Error in handleAddVote: ", e);
            sendErrorMessage(session, "An error occurred while adding the vote.", e);
        }
    }


    private void handleClearVotes(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID sessionId = UUID.fromString(payload.get("sessionId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

            voteService.clearVotes(sessionId);
            voteSessionService.hideVotes(sessionId);
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "votesCleared",
                    "sessionId", sessionId.toString()
            ));
        } catch (Exception e) {
            logger.error("Error in handleClearVotes: ", e);
            sendErrorMessage(session, "An error occurred while clearing the votes.", e);
        }
    }

    private void handleRevealVotes(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));

        try {
            voteSessionService.revealVotes(sessionId);
            playerService.recordAction(senderPlayerId);

            var voteList = voteService.getVotesForSession(sessionId);
            String calculatedResult = calculateVoteResult(voteList);

            broadcastToRoom(roomId, Map.of(
                    "type", "votesRevealed",
                    "votes", voteList,
                    "result", calculatedResult
            ));
        } catch (IllegalStateException e) {
            // Send error only to the client who triggered the action
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                    "type", "error",
                    "message", "You cannot reveal votes if no one has voted."
            ))));
        } catch (IllegalArgumentException e) {
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Vote session not found."
            ))));
        }
    }

    private String calculateVoteResult(List<VoteDTO> votes) {
        Map<String, Integer> counts = new HashMap<>();
        String result = "";
        int max = 0;

        for (VoteDTO vote : votes) {
            String value = vote.cardValue();
            counts.put(value, counts.getOrDefault(value, 0) + 1);

            if (counts.get(value) > max) {
                max = counts.get(value);
                result = value;
            }
        }

        return result.isEmpty() ? null : result;
    }

    private void handleEndVoteSession(WebSocketSession session, Map<String, String> payload) {
        try {
            UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
            UUID sessionId = UUID.fromString(payload.get("sessionId"));
            UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
            String finalValue = payload.get("finalValue");

            VoteSession endedSession = voteService.endVoteSession(sessionId, finalValue);
            StoryDTO endedStory = storyService.getStoryById(endedSession.getStory().getId());
            playerService.recordAction(senderPlayerId);

            broadcastToRoom(roomId, Map.of(
                    "type", "voteEnded",
                    "finalValue", finalValue,
                    "story", endedStory
            ));
        } catch (Exception e) {
            logger.error("Error in handleEndVoteSession: ", e);
            sendErrorMessage(session, "An error occurred while ending the vote session.", e);
        }
    }



    private void handleUpdateRoomSettings(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));

        boolean allowQuestionMark = Boolean.parseBoolean(String.valueOf(payload.get("allowQuestionMark")));
        boolean allowVoteModification = Boolean.parseBoolean(String.valueOf(payload.get("allowVoteModification")));

        RoomSettingsDTO settingsRequest = new RoomSettingsDTO();
        settingsRequest.setAllowQuestionMark(allowQuestionMark);
        settingsRequest.setAllowVoteModification(allowVoteModification);

        var senderPlayer = playerService.findById(senderPlayerId);
        var user = userService.findById(senderPlayer.getUser().getId());

        try {
            RoomSettingsDTO updatedSettings = roomService.changeRoomSettings(roomId, settingsRequest, user.getId());

            broadcastToRoom(roomId, Map.of(
                    "type", "roomSettingsUpdated",
                    "settings", updatedSettings
            ));
        } catch (UnauthorizedActionException e) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
        }
    }



    private void broadcastToRoom(UUID roomId, Object payload) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);

        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastToRoomExceptSession(UUID roomId, WebSocketSession excludeSession, Object payload) {
        List<WebSocketSession> sessions = roomSessions.get(roomId);

        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                // Trimite mesajul doar dacă sesiunea nu este cea care a votat
                if (!session.equals(excludeSession) && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
                    } catch (IOException e) {
                        logger.error("Error sending message to session {}", session.getId(), e);
                    }
                }
            }
        }
    }

    private void broadcastRoomDetails(RoomDetailsDTO room) throws Exception {
        List<PlayerDTO> players = room.getPlayers();
        String message = objectMapper.writeValueAsString(Map.of("type", "userJoined", "players", players));

        logger.info("Broadcasting message: {}", message);

        for (WebSocketSession session : sessionUserMap.keySet()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Connection is closed {} si {}", session.getAttributes().get("roomId"),session.getAttributes().get("userId"));
        UUID userId = UUID.fromString(session.getAttributes().get("userId").toString());
        UUID roomId = UUID.fromString(session.getAttributes().get("roomId").toString());

        if (userId != null && roomId != null) {
            logger.info("User disconnected: " + userId);
            PlayerDTO disconnectedPlayer = playerService.disconnectUserFromRoom(roomId, userId);

            List<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
            }
            if (disconnectedPlayer != null) {
                broadcastToRoom(roomId, Map.of(
                        "type", "playerDisconnected",
                        "player", disconnectedPlayer
                ));
            }
        }
        sessionUserMap.remove(session);
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage, Throwable throwable) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "error");
            errorResponse.put("message", errorMessage);

            String details = (throwable.getMessage() != null)
                    ? throwable.getMessage()
                    : (throwable.getCause() != null && throwable.getCause().getMessage() != null)
                    ? throwable.getCause().getMessage()
                    : throwable.toString();
            errorResponse.put("details", details);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        } catch (IOException e) {
            logger.error("Error sending error message: ", e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        sendErrorMessage(session, errorMessage, new RuntimeException("No exception provided"));
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }


    private record RoomDataResponse(UUID roomId, String roomName, List<Player> players) {}
}
