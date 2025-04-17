package com.example.PlanItPoker.model;

import com.example.PlanItPoker.model.enums.PlayerRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;


import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "user_id"})
})
public class Player {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerRole role;

    @Column(name = "has_voted")
    private boolean hasVoted;

    @Column(name = "is_connected")
    private boolean isConnected;

    @Column(name = "vote")
    private String vote;

    // Getters, Setters, Constructors
}
