package User;
public class Bidder extends User {

    public Bidder(String username, String password, int id) {
        super(username, password, id);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Bidder");
    }

    public void placeBid() {
        System.out.println(username + " placed a bid");
    }
}