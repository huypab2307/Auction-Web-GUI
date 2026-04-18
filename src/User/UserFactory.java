package User;
public class UserFactory {

    public static User createUser(Role role, String username, String password) {
        switch (role) {
            case BIDDER:
                return new Bidder(username, password,1);
            case SELLER:
                return new Seller(username, password,1);
            case ADMIN:
                return new Admin(username, password,1);
            default:
                throw new IllegalArgumentException("Invalid role");
        }
    }
}