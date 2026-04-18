package User;
public class Admin extends User {

    public Admin(String username, String password, int id) {
        super(username, password, id);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is an Admin");
    }

    public void manageSystem() {
        System.out.println(username + " is managing system");
    }
}