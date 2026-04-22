package User;

import Auction.AuctionManager;

public class Bidder extends User {

    public Bidder(String username, String password, int id) {
        super(username, password, id,Role.BIDDER);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Bidder");
    }

    public boolean placeBid(int id, double oldPrice) {
        return AuctionManager.getInstance().placebid(this, id,oldPrice);
    }
}