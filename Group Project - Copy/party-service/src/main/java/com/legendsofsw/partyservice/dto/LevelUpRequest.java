package com.legendsofsw.partyservice.dto;

public class LevelUpRequest {

    private String chosenClass;

    public LevelUpRequest() {
    }

    public LevelUpRequest(String chosenClass) {
        this.chosenClass = chosenClass;
    }

    public String getChosenClass() { return chosenClass; }
    public void setChosenClass(String chosenClass) { this.chosenClass = chosenClass; }
}
