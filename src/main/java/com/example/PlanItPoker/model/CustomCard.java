package com.example.PlanItPoker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties("room")
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
    @ToString.Exclude
    @JsonBackReference
    private Room room;

    // Getters, Setters, Constructori
}
