package User;

public abstract class User {
    protected String username;
    protected String password;
    protected int id;

    public User(String username, String password, int id) {
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public User changeRole(Role newRole) {
        return UserFactory.createUser(newRole, this.username, this.password);
    }

    public abstract void showRole();
}