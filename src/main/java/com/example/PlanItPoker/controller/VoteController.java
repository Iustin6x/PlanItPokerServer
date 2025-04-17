package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.DTOs.RoomDTO;
import com.example.PlanItPoker.payload.request.VoteRequest;
import com.example.PlanItPoker.service.RoomService;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VoteController {
    private final RoomService roomService;
    private final UserServiceImpl userService;


    private final JwtUtil jwtUtil;


//    @PostMapping("/vote")
//    public ResponseEntity<Void> submitVote(
//            @RequestBody VoteRequest request,
//            @RequestHeader("Authorization") String authHeader
//    ) {
//        String token = authHeader.substring(7); // Remove "Bearer " prefix
//        UUID userId = jwtUtil.extractUserId(token);
//        User user = userService.findById(userId);
//        votingService.submitVote(request.sessionId(), user.getId(), request.cardValue());
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/results/{sessionId}")
//    public ResponseEntity<Map<String, Long>> getResults(@PathVariable UUID sessionId) {
//        return ResponseEntity.ok(votingService.revealVotes(sessionId));
//    }
}
