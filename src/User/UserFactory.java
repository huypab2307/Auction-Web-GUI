package User;
public class UserFactory {

    public static User createUser(Role role, User user) {
        switch (role) {
            case BIDDER:
                return new Bidder(user.getUsername(), user.getPassword(),user.getId());
            case SELLER:
                return new Seller(user.getUsername(), user.getPassword(),user.getId());
            case ADMIN:
                return new Admin(user.getUsername(), user.getPassword(),user.getId());
            default:
                throw new IllegalArgumentException("Invalid role");
        }
    }
}