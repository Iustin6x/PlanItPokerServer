package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.service.RoomService;
import com.example.PlanItPoker.service.impl.UserServiceImpl;
import com.example.PlanItPoker.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final RoomService roomService;
    private final UserServiceImpl userService;


    private final JwtUtil jwtUtil;
}
