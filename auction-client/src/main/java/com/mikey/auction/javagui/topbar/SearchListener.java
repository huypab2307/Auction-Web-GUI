package com.mikey.auction.javagui.topbar;

import com.mikey.auction.dto.AuctionInfo;

import javafx.scene.control.MenuItem;

import java.util.ArrayList;



public interface SearchListener {
    ArrayList<MenuItem> notifications = new ArrayList<>();
    
    void onSearchPerformed(ArrayList<AuctionInfo> results);
    
}