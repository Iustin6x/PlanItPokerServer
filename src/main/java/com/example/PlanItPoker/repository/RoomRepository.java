package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository  extends JpaRepository<Room, UUID> {
    @Query("SELECT r FROM Room r JOIN r.players p WHERE p.user.id = :userId")
    List<Room> findAllByUserId(@Param("userId") UUID userId);

}
