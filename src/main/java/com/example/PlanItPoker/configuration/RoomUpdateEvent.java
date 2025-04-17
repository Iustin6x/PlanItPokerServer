package com.example.PlanItPoker.configuration;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class RoomUpdateEvent extends ApplicationEvent {
    private final UUID roomId;

    public RoomUpdateEvent(Object source, UUID roomId) {
        super(source);
        this.roomId = roomId;
    }

    public UUID getRoomId() {
        return roomId;
    }
}
