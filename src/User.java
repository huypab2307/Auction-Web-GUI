public abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
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