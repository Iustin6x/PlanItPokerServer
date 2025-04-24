package com.example.PlanItPoker.configuration;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.model.VoteSession;
import com.example.PlanItPoker.model.enums.PlayerRole;
import com.example.PlanItPoker.model.enums.StoryStatus;
import com.example.PlanItPoker.payload.DTOs.*;
import com.example.PlanItPoker.service.*;
import com.example.PlanItPoker.service.impl.PlayerServiceImpl;
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

    // Mapare sesiuni WebSocket → userId (sau alt identificator)
    private final Map<WebSocketSession, UUID> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<UUID, List<WebSocketSession>> roomSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        System.out.println("User connected with id: " + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = payload.get("type");

        if ("join".equals(type)) {
            handleJoin(session, payload);
        } else if ("changeRole".equals(type)) {
            handleChangeRole(session, payload);
        } else if ("changeName".equals(type)) {
            handleChangeName(session, payload);
        } else if ("createStory".equals(type)) {
            handleCreateStory(session, payload);
        } else if ("updateStory".equals(type)) {
            handleUpdateStory(session, payload);
        }else if ("deleteStory".equals(type)) {
            handleDeleteStory(session, payload);
        } else if ("getStories".equals(type)) {
            handleGetStories(session, payload);
        } else if ("startVote".equals(type)) {
            handleStartVote(session, payload);
        } else if ("skipVote".equals(type)) {
            handleSkipStory(session, payload);
        } else if ("addVote".equals(type)) {
            handleAddVote(session, payload);
        } else if ("revealVotes".equals(type)) {
            handleRevealVotes(session, payload);
        } else if ("clearVotes".equals(type)) {
            handleClearVotes(session, payload);
        } else if ("endVoteSession".equals(type)) {
            handleEndVoteSession(session, payload);
        }   else if ("getStoryWithSession".equals(type)) {
            handleGetStoryWithSession(session, payload);
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, String> payload) throws IOException {
        logger.info("Received join message");

        UUID roomId = UUID.fromString(payload.get("roomId"));
        String userIdStr = (String) session.getAttributes().get("userId");
        UUID userId = UUID.fromString(userIdStr);

        // 1. Adauga in camera
        PlayerDTO player = playerService.joinRoom(roomId, userId);
        UUID playerId = player.id();
        session.getAttributes().put("playerId", playerId.toString());

        // 2. Obținem RoomInfo + altele prin servicii
        RoomInfoDTO roomInfo = roomService.getRoomInfo(roomId);
        List<PlayerDTO> players = playerService.getPlayersByRoomId(roomId)
                .stream()
                .map(PlayerDTO::fromEntity) // dacă ai DTO direct, scapi de asta
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
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "voteSession",
                    "session", voteSession
            ))));
        } else {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "noActiveVoteSession"
            ))));
        }

        broadcastToRoom(roomId, Map.of(
                "type", "playerJoined",
                "player", player
        ));
    }


    private void handleChangeName(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID senderPlayerId = UUID.fromString((String) session.getAttributes().get("playerId"));
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        UUID targetPlayerId = UUID.fromString(payload.get("playerId"));
        String newName = payload.get("newName");

        var senderPlayer = playerService.findById(senderPlayerId);
        var targetPlayer = playerService.findById(targetPlayerId);

        // Dacă încercă să schimbe alt player și nu e moderator, refuză
        if (!senderPlayerId.equals(targetPlayerId) && senderPlayer.getRole() != PlayerRole.MODERATOR) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Not allowed to change another player's name\"}"));
            return;
        }

        playerService.updatePlayerName(targetPlayerId, newName);

        // Trimite un mesaj către toți utilizatorii din cameră
        broadcastToRoom(roomId, Map.of(
                "type", "playerNameChanged",
                "playerId", targetPlayerId,
                "newName", newName
        ));
    }


    private void handleChangeRole(WebSocketSession session, Map<String, String> payload) throws IOException {
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

        broadcastToRoom(roomId, Map.of(
                "type", "playerRoleChanged",
                "playerId", targetPlayerId,
                "newRole", newRole.toString()
        ));
    }

    private void handleCreateStory(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        StoryDTO createdStory = storyService.createStory(roomId, payload.get("name"));

        broadcastToRoom(roomId, Map.of(
                "type", "storyCreated",
                "story", createdStory
        ));
    }

    private void handleGetStoryWithSession(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID storyId = UUID.fromString(payload.get("storyId"));

        StoryDTO story = storyService.getStoryById(storyId);

        VoteSessionDTO voteSession = voteSessionService.getSessionByStoryId(storyId);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "storyWithSession");
        response.put("story", story);

        if (voteSession != null) {
            response.put("session", voteSession);
        } else {
            response.put("session", null);
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleUpdateStory(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID storyId = UUID.fromString(payload.get("storyId"));

        String storyName = payload.get("name");
        if (storyName == null || storyName.isEmpty()) {
            throw new IllegalArgumentException("Story name cannot be null or empty.");
        }

        StoryDTO updatedStory = storyService.updateStoryName(storyId, storyName);

        broadcastToRoom(roomId, Map.of(
                "type", "storyUpdated",
                "story", updatedStory
        ));
    }

    private void handleDeleteStory(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID storyId = UUID.fromString(payload.get("storyId"));

        storyService.deleteStory(storyId);

        broadcastToRoom(roomId, Map.of(
                "type", "storyDeleted",
                "storyId", storyId.toString()
        ));
    }

    private void handleGetStories(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        List<StoryDTO> stories = storyService.getStoriesByRoomId(roomId);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "storyList",
                "stories", stories
        ))));
    }


    private void handleSkipStory(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID storyId = UUID.fromString(payload.get("storyId"));

        StoryDTO skippedStory = storyService.updateStoryStatus(storyId, StoryStatus.SKIPPED);


        voteSessionService.deleteSessionForStory(storyId);


        StoryDTO nextStory = storyService.getNextStoryToVote(roomId);

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
    }


    private void handleStartVote(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        StoryDTO nextStory = storyService.getNextStoryToVote(roomId);

        if (nextStory == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"No available story to vote\"}"));
            return;
        }

        VoteSessionDTO voteSession = voteSessionService.createSession(nextStory.id(), roomId);

        broadcastToRoom(roomId, Map.of(
                "type", "voteStarted",
                "session", voteSession
        ));
    }

    private void handleAddVote(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID playerId = UUID.fromString((String) session.getAttributes().get("playerId"));
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        String cardValue = payload.get("cardValue");

        var voteDTO = voteService.addVote(sessionId, playerId, cardValue);

        broadcastToRoom(roomId, Map.of(
                "type", "voteAdded",
                "vote", voteDTO
        ));

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
    }

    private void handleClearVotes(WebSocketSession session, Map<String, String> payload) {
        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        voteService.clearVotes(sessionId);
        voteSessionService.hideVotes(sessionId);

        broadcastToRoom(roomId, Map.of(
                "type", "votesCleared",
                "sessionId", sessionId.toString()
        ));
    }

    private void handleRevealVotes(WebSocketSession session, Map<String, String> payload) throws IOException {
        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));

        try {
            voteSessionService.revealVotes(sessionId);

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
        UUID roomId = UUID.fromString((String) session.getAttributes().get("roomId"));
        UUID sessionId = UUID.fromString(payload.get("sessionId"));
        String finalValue = payload.get("finalValue");

        VoteSession endedSession = voteService.endVoteSession(sessionId, finalValue);

        broadcastToRoom(roomId, Map.of(
                "type", "voteEnded",
                "finalValue", finalValue
        ));
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
                // Trimite mesaj la toți ceilalți din cameră
                broadcastToRoom(roomId, Map.of(
                        "type", "playerDisconnected",
                        "player", disconnectedPlayer
                ));
            }
        }

        sessionUserMap.remove(session);
    }



    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // Poți loga erorile
        exception.printStackTrace();
    }


    // DTO pentru trimis datele camerei
    private record RoomDataResponse(UUID roomId, String roomName, List<Player> players) {}
}
