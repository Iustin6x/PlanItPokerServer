package com.example.PlanItPoker.payload.request;

import com.example.PlanItPoker.model.enums.CardType;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
import java.util.List;


import java.util.List;

public record RoomRequest(
//        @NotBlank(message = "Room name cannot be blank")
        String name,

//        @NotNull(message = "Card type cannot be null")
        CardType cardType,

//        List<@NotBlank(message = "Custom card value cannot be blank") String> customCards
        List<String> cards
) {}