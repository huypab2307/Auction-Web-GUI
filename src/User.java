public class User extends Person {
    Role role;

    public User(String username, String password) {
        super(username, password);
        this.role = Role.BIDDER;
    }
    public void updateRole(ROLE role){
        
    }
}
