package com.example.PlanItPoker.service.impl;

import com.example.PlanItPoker.model.CustomCard;
import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.enums.CardType;
import com.example.PlanItPoker.service.CardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {
    public List<String> getCardsForRoom(Room room) {
        return room.getAllCards();
    }

    public void updateCustomCards(Room room, List<String> newCards) {
        if (room.getCardType() != CardType.CUSTOM) {
            throw new IllegalStateException("Room is not using custom cards");
        }

        room.getCustomCards().clear();
        newCards.forEach(value -> {
            CustomCard card = new CustomCard();
            card.setValue(value);
            card.setRoom(room);
            room.getCustomCards().add(card);
        });
    }
}
