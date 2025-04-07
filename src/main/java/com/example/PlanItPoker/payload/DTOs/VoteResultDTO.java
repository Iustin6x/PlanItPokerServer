package com.example.PlanItPoker.payload.DTOs;

public record VoteResultDTO(
        String cardValue,
        Long count,
        Double percentage
) {}
