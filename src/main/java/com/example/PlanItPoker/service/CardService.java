package com.example.PlanItPoker.service;

import com.example.PlanItPoker.model.Room;

import java.util.List;

public interface CardService {
    List<String> getCardsForRoom(Room room);
    void updateCustomCards(Room room, List<String> newCards);
}
