package com.legendsofsw.partyservice.dto;

public class CreateHeroRequest {

    private String name;
    private String heroClass;

    public CreateHeroRequest() {
    }

    public CreateHeroRequest(String name, String heroClass) {
        this.name = name;
        this.heroClass = heroClass;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHeroClass() { return heroClass; }
    public void setHeroClass(String heroClass) { this.heroClass = heroClass; }
}
