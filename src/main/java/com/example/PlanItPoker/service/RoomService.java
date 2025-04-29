package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.payload.DTOs.RoomListDTO;
import com.example.PlanItPoker.payload.DTOs.RoomResponseDTO;
import com.example.PlanItPoker.payload.DTOs.RoomSettingsDTO;
import com.example.PlanItPoker.payload.request.RoomRequest;

import java.util.List;
import java.util.UUID;


public interface RoomService {
    RoomListDTO createRoom(RoomRequest request, UUID creatorId);

    List<RoomListDTO> getUserRooms(UUID userId);

    void deleteRoom(UUID roomId);

    Room updateRoom(UUID roomId, RoomRequest request, UUID userId);

    Room getRoomById(UUID roomId);

    RoomResponseDTO getRoomInfo(UUID roomId);

    RoomSettingsDTO changeRoomSettings(UUID roomId, RoomSettingsDTO settingsRequest, UUID userId);
}
