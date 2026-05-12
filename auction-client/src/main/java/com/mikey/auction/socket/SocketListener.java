package com.mikey.auction.socket;

public interface SocketListener {
    void onResponseReceived(String category, String action, String jsonData);
}