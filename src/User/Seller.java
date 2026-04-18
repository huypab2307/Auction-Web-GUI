package User;
public class Seller extends User {

    public Seller(String username, String password, int id) {
        super(username, password, id);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

    public void sellItem() {
        System.out.println(username + " is selling an item");
    }
}