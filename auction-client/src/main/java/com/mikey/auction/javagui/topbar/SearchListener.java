package com.mikey.auction.javagui.topbar;

import com.mikey.auction.dto.AuctionInfo;

import java.util.ArrayList;



public interface SearchListener {
    void onSearchPerformed(ArrayList<AuctionInfo> results);
}