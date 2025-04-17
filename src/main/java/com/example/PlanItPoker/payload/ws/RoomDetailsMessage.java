package com.example.PlanItPoker.payload.ws;

import com.example.PlanItPoker.model.Player;
import com.example.PlanItPoker.model.Story;
import com.example.PlanItPoker.model.VoteSession;
import lombok.Data;

import java.util.List;

@Data
public class RoomDetailsMessage {
    private String type = "roomDetails";
    private List<Player> players;
    private List<Story> stories;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

//    public VoteSession getVoteSession() {
//        return voteSession;
//    }
//
//    public void setVoteSession(VoteSession voteSession) {
//        this.voteSession = voteSession;
//    }

    public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
}
