package com.example.PlanItPoker.controller;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping("/room")
    public Room postRoom(@RequestBody Room room){
        return roomService.postRoom(room);
    }



}
