package com.example.PlanItPoker.exception;

import java.util.UUID;

public class StoryNotFoundException extends RuntimeException {
    public StoryNotFoundException(UUID storyId) {
        super("Story not found with id: " + storyId);
    }
}
