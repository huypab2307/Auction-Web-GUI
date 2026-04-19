package User;

import Auction.AuctionManager;
import Database.AuctionDAO;
import Items.FactoryItem;
import Items.Item;

public class Seller extends User {

    public Seller(String username, String password, int id) {
        super(username, password, id,Role.SELLER);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

    public <T> T createItem(Class<T> clazz, String name, String desc) {
        return FactoryItem.createBuilder(clazz, name, desc, this.id);
    }
    public void uploadItem(Item item, double price, double stepPrice, int durationDays) {
        AuctionManager.getAuction().uploadItem(item, price, stepPrice, durationDays, this);
    }
}