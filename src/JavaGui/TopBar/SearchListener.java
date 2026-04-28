package JavaGui.TopBar;

import Auction.AuctionInfo;

import java.util.ArrayList;

public interface SearchListener {
    void onSearchPerformed(ArrayList<AuctionInfo> results);
}