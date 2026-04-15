public class Seller extends User {

    public Seller(String username, String password) {
        super(username, password);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

    public void sellItem() {
        System.out.println(username + " is selling an item");
    }
}