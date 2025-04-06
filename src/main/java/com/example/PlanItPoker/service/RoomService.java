package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    public Room postRoom(Room room){
        return roomRepository.save(room);
    }

}
