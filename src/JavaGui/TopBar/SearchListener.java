package JavaGui.TopBar;

import java.util.ArrayList;

import DTO.AuctionInfo;

public interface SearchListener {
    void onSearchPerformed(ArrayList<AuctionInfo> results);
}