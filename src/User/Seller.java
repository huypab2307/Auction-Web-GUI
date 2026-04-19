package User;

import Items.FactoryItem;

public class Seller extends User {

    public Seller(String username, String password, int id) {
        super(username, password, id,Role.SELLER);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

    public <T> T sellItem(Class<T> clazz, String name, String desc, double price) {
        return FactoryItem.createBuilder(clazz, name, desc, price, this.id);
    }
}