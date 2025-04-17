package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.User;
import com.example.PlanItPoker.payload.DTOs.RoomDTO;
import com.example.PlanItPoker.payload.DTOs.RoomDetailsDTO;
import com.example.PlanItPoker.payload.DTOs.RoomInfoDTO;
import com.example.PlanItPoker.payload.request.RoomRequest;
import com.example.PlanItPoker.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


public interface RoomService {
    RoomDTO createRoom(RoomRequest request, UUID creatorId);

    List<RoomDTO> getUserRooms(UUID userId);

    void deleteRoom(UUID roomId);

    RoomDTO updateRoom(UUID roomId, RoomRequest request, UUID userId);

    Room getRoomById(UUID roomId);

    RoomDetailsDTO getRoomDetails(UUID roomId);

    RoomInfoDTO getRoomInfo(UUID roomId);

}
