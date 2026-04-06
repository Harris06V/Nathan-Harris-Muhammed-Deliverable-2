package com.legendsofsw.playerservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;

@Entity
@Table(name = "saved_parties")
public class SavedParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    private String name;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String partyDataJson;

    public SavedParty() {
    }

    public SavedParty(Long playerId, String name, String partyDataJson) {
        this.playerId = playerId;
        this.name = name;
        this.partyDataJson = partyDataJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartyDataJson() {
        return partyDataJson;
    }

    public void setPartyDataJson(String partyDataJson) {
        this.partyDataJson = partyDataJson;
    }
}
