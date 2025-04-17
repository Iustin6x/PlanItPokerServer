package com.example.PlanItPoker.exception;

public class PlayerAlreadyJoinedException extends RuntimeException {
    public PlayerAlreadyJoinedException(String message) {
        super(message);
    }
}
