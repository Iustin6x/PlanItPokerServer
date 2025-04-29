package com.example.PlanItPoker.repository;

import com.example.PlanItPoker.model.Room;
import com.example.PlanItPoker.model.RoomSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomSettingsRepository   extends JpaRepository<RoomSettings, UUID> {


}
