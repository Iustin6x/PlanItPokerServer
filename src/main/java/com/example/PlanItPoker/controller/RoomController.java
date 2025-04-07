package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.DTOs.RoomDTO;
import com.example.PlanItPoker.payload.request.RoomRequest;
import com.example.PlanItPoker.security.JwtRequestFilter;
import com.example.PlanItPoker.service.AuthService;
import com.example.PlanItPoker.service.RoomService;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RoomController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final UserServiceImpl userService;


    private final JwtUtil jwtUtil;

    @Autowired
    public RoomController(RoomService roomService, UserServiceImpl userService, JwtUtil jwtUtil) {
        this.roomService = roomService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDTO>> getUserRooms(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId);
        logger.info("User: " + user.toString());
        return ResponseEntity.ok(roomService.getUserRooms(user.getId()));
    }


    @PostMapping("/room")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomRequest request, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId);
        logger.info("Creating a new room",user.getId());
        return ResponseEntity.ok(roomService.createRoom(request, user.getId()));
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDTO> updateRoom(
            @PathVariable UUID roomId,
            @RequestBody RoomRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = jwtUtil.extractUserId(token);
        User user = userService.findById(userId);

        RoomDTO updatedRoom = roomService.updateRoom(roomId, request, user.getId());

        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable UUID roomId)
    {

        roomService.deleteRoom(roomId);

        return ResponseEntity.noContent().build();
    }

}
