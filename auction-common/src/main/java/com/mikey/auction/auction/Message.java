package com.mikey.auction.auction;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private LocalDateTime timeSend;
    private String context;

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public LocalDateTime getTimeSend() {
        return timeSend;
    }

    public String getContext() {
        return context;
    }

    public Message(int id, int senderId, int receiverId,String context, LocalDateTime timeSend) {
        this.id = id;
        this.context = context;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.timeSend = timeSend;
    }  
}
