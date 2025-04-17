package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.enums.CardType;

import java.util.List;

public class RoomInfoDTO {

    private String name;
    private CardType cardType;
    private List<String> customCards;

    public RoomInfoDTO(String name, CardType cardType, List<String> customCards) {
        this.name = name;
        this.cardType = cardType;
        this.customCards = customCards;
    }

    public static RoomInfoDTO fromEntity(Room room) {
        List<String> cards = room.getCustomCards()
                .stream()
                .map(card -> card.getValue())
                .toList();
        return new RoomInfoDTO(room.getName(), room.getCardType(), cards);
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
}