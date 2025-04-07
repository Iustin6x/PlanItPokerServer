package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.DTOs.RoomDTO;
import com.example.PlanItPoker.service.RoomService;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final RoomService roomService;
    private final UserServiceImpl userService;


    private final JwtUtil jwtUtil;
}
