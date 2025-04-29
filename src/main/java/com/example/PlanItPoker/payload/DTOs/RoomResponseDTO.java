package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.enums.CardType;

import java.util.List;

public class RoomResponseDTO {


    private String name;
    private CardType cardType;
    private List<String> customCards;
    private RoomSettingsDTO roomSettings;

    public RoomResponseDTO(String name, CardType cardType, List<String> customCards, RoomSettingsDTO roomSettings) {
        this.name = name;
        this.cardType = cardType;
        this.customCards = customCards;
        this.roomSettings = roomSettings;
    }

    public static RoomResponseDTO fromEntity(Room room) {
        List<String> cards = room.getCustomCards()
                .stream()
                .map(card -> card.getValue())
                .toList();
        RoomSettingsDTO roomSettingsDTO = RoomSettingsDTO.fromEntity(room.getRoomSettings());
        return new RoomResponseDTO(room.getName(), room.getCardType(), cards, roomSettingsDTO);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public List<String> getCustomCards() {
        return customCards;
    }

    public void setCustomCards(List<String> customCards) {
        this.customCards = customCards;
    }

    public RoomSettingsDTO getRoomSettings() {
        return roomSettings;
    }

    public void setRoomSettings(RoomSettingsDTO roomSettings) {
        this.roomSettings = roomSettings;
    }
}