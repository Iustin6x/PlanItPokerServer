package com.example.PlanItPoker.payload.request;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.RoomSettings;
import com.example.PlanItPoker.model.enums.CardType;
import com.example.PlanItPoker.payload.DTOs.RoomSettingsDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoomRequest {

    private UUID id; // ➡️ nou adăugat

    @NotBlank(message = "Room name cannot be blank")
    private String name;

    @NotNull(message = "Card type cannot be null")
    private CardType cardType;

    private List<@NotBlank(message = "Custom card value cannot be blank") String> cards;

    @NotNull(message = "RoomSettings cannot be null")
    private RoomSettingsDTO roomSettings;

    public RoomRequest() {
    }

    public RoomRequest(UUID id, String name, CardType cardType, List<String> cards, RoomSettingsDTO roomSettings) {
        this.id = id;
        this.name = name;
        this.cardType = cardType;
        this.cards = cards;
        this.roomSettings = roomSettings;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public List<String> getCards() {
        return cards;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }

    public RoomSettingsDTO getRoomSettings() {
        return roomSettings;
    }

    public void setRoomSettings(RoomSettingsDTO roomSettings) {
        this.roomSettings = roomSettings;
    }

    public static RoomRequest fromEntity(Room room) {
        List<String> customCards = room.getCustomCards()
                .stream()
                .map(card -> card.getValue())
                .collect(Collectors.toList());

        RoomSettingsDTO settingsDTO = RoomSettingsDTO.fromEntity(room.getRoomSettings());

        return new RoomRequest(
                room.getId(), // ➡️ aici adaugi id-ul camerei
                room.getName(),
                room.getCardType(),
                customCards,
                settingsDTO
        );
    }

    public Room toEntity() {
        Room room = new Room();
        room.setId(this.id); // ➡️ setează id-ul dacă există
        room.setName(this.name);
        room.setCardType(this.cardType);

        if (this.cards != null) {
            this.cards.forEach(room::addCustomCard);
        }

        RoomSettings settings = new RoomSettings();
        settings.setAllowQuestionMark(this.roomSettings.isAllowQuestionMark());
        settings.setAllowVoteModification(this.roomSettings.isAllowVoteModification());

        room.setRoomSettings(settings);

        return room;
    }
}