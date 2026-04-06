package com.legendsofsw.playerservice.dto;

public class LoginResponse {

    private Long playerId;
    private String username;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(Long playerId, String username, String message) {
        this.playerId = playerId;
        this.username = username;
        this.message = message;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
