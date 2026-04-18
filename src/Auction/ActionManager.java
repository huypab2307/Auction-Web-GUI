package Auction;

class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getAction(){
        return instance;
    }
    
}
