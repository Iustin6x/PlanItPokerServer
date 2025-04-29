package com.example.PlanItPoker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_settings")
public class RoomSettings {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "allow_question_mark", nullable = false)
    private boolean allowQuestionMark = true;  // Implicit este `true`, adică se permite utilizarea `?`

    @Column(name = "allow_vote_modification", nullable = false)
    private boolean allowVoteModification;

    @OneToOne(mappedBy = "roomSettings")
    private Room room;

}
