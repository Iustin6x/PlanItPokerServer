package com.example.PlanItPoker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)") // Store UUID as 16-byte binary
    private UUID id;


    @Column(nullable = false)
    private String name;


    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    @JsonProperty("isGuest") // Adaugă această anotație
    private boolean isGuest;

    @Column
    private String avatar;

    public User(String name) {
        this.name = name;
        this.isGuest = true;
    }

    public <T> User(String email, String password, List<T> ts) {

    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGuest(boolean guest) {
        isGuest = guest;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


}