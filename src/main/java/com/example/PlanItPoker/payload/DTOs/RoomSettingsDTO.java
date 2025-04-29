package com.example.PlanItPoker.payload.DTOs;

import com.example.PlanItPoker.model.RoomSettings;


public class RoomSettingsDTO {
    private boolean allowQuestionMark;
    private boolean allowVoteModification;

    public RoomSettingsDTO(boolean allowQuestionMark, boolean allowVoteModification) {
        this.allowQuestionMark = allowQuestionMark;
        this.allowVoteModification = allowVoteModification;
    }

    public RoomSettingsDTO() {

    }


    public static RoomSettingsDTO fromEntity(RoomSettings roomSettings) {
        return new RoomSettingsDTO(roomSettings.isAllowQuestionMark(), roomSettings.isAllowVoteModification());
    }

    public RoomSettings toEntity() {
        RoomSettings roomSettings = new RoomSettings();
        roomSettings.setAllowQuestionMark(this.allowQuestionMark);
        roomSettings.setAllowVoteModification(this.allowVoteModification);
        return roomSettings;
    }


    public boolean isAllowQuestionMark() {
        return allowQuestionMark;
    }

    public void setAllowQuestionMark(boolean allowQuestionMark) {
        this.allowQuestionMark = allowQuestionMark;
    }

    public boolean isAllowVoteModification() {
        return allowVoteModification;
    }

    public void setAllowVoteModification(boolean allowVoteModification) {
        this.allowVoteModification = allowVoteModification;
    }
}
