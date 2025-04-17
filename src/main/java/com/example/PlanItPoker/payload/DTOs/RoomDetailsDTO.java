package com.example.PlanItPoker.payload.DTOs;



import com.example.PlanItPoker.model.CustomCard;
import com.example.PlanItPoker.model.enums.CardType;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailsDTO {


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public List<String> getCustomCards() {
        return customCards;
    }

    public void setCustomCards(List<String> customCards) {
        this.customCards = customCards;
    }

    private String name;
    private CardType cardType;
    private List<String> customCards;
    private List<PlayerDTO> players;
    private List<StoryDTO> stories;
    private VoteSessionDTO voteSession;

    // Constructor
    public RoomDetailsDTO(String name, CardType cardType, List<String> customCards, List<PlayerDTO> players, List<StoryDTO> stories, StoryDTO currentStory, VoteSessionDTO voteSession) {
        this.name = name;
        this.cardType = cardType;
        this.customCards = customCards;
        this.players = new ArrayList<>(players);
        this.stories = stories;
        this.voteSession = voteSession;
    }

    // Getters and Setters
    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = new ArrayList<>(players);  // Asigură-te că folosești o colecție modificabilă
    }

    public List<StoryDTO> getStories() {
        return stories;
    }

    public void setStories(List<StoryDTO> stories) {
        this.stories = stories;
    }


    public VoteSessionDTO getVoteSession() {
        return voteSession;
    }

    public void setVoteSession(VoteSessionDTO voteSession) {
        this.voteSession = voteSession;
    }
}
