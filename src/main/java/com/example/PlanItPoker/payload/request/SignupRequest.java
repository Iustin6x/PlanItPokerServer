package com.example.PlanItPoker.payload.request;

import java.util.UUID;

public class SignupRequest {
    private String email;

    private String name;

    private String password;

    private UUID guestUserId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getGuestUserId() {
        return guestUserId;
    }

    public void setGuestUserId(UUID guestUserId) {
        this.guestUserId = guestUserId;
    }
}
