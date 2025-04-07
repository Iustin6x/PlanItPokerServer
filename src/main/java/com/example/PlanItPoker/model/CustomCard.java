package com.example.PlanItPoker.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "custom_cards")
public class CustomCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value; // Stochează valori ca șiruri: "0.5", "?", "Coffee"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Getters, Setters, Constructori
}
