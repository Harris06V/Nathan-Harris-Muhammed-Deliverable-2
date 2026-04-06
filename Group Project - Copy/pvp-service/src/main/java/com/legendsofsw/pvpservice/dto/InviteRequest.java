package com.legendsofsw.pvpservice.dto;

public class InviteRequest {

    private Long senderId;
    private String senderUsername;
    private String receiverUsername;

    public InviteRequest() {
    }

    public InviteRequest(Long senderId, String senderUsername, String receiverUsername) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
    }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
}
