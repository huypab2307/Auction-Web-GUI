package JavaGui.TopBar;

import JustToDisplayInfo.AuctionInfo;

import java.util.ArrayList;

public interface SearchListener {
    void onSearchPerformed(ArrayList<AuctionInfo> results);
}