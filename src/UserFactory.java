public class UserFactory {

    public static User createUser(Role role, String username, String password) {
        switch (role) {
            case BIDDER:
                return new Bidder(username, password);
            case SELLER:
                return new Seller(username, password);
            case ADMIN:
                return new Admin(username, password);
            default:
                throw new IllegalArgumentException("Invalid role");
        }
    }
}