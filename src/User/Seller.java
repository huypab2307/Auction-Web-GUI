package User;

import Auction.AuctionManager;
import Items.FactoryItem;
import Items.Item;
import Items.ItemType;

public class Seller extends User {

    public Seller(String username, String password, int id) {
        super(username, password, id,Role.SELLER);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

    public Item createItem(ItemType type, String name, String desc, String imagePath) {
        return FactoryItem.createBuilder(type, name, desc, this.id,imagePath);
    }
    public void uploadItem(Item item, double price, double stepPrice, int durationDays) {
        AuctionManager.getInstance().uploadItem(item, price, stepPrice, durationDays, this);
    }
}