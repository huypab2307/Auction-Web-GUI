public class User extends Person {
    Role role;

    public User(String username, String password, Role role) {
        super(username, password);
        this.role = role;
    }
}
