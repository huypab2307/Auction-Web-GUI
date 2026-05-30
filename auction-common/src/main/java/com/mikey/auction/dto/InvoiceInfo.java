package com.mikey.auction.dto;

import java.time.LocalDateTime;

public class InvoiceInfo {
    private int id;
    private int auctionId;
    private int winnerId;
    private double amount;
    private String status; // 'AWAITING_PAYMENT', 'PAID', 'CANCELLED'
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public InvoiceInfo(int id, int auctionId, int winnerId, double amount, String status, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.auctionId = auctionId;
        this.winnerId = winnerId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public InvoiceInfo() {
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }




}