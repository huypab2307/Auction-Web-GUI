import Auction.*;
import Database.UserDAO;
import User.*;

class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getInstance();
        UserDAO users = UserDAO.getInstance();

        // users.register("togedemaru","Matkhau");
        // users.register("jenny", "Matkhau");
        
        Bidder user1 = (Bidder) users.login("jenny","Matkhau");
        Bidder user2 = (Bidder) users.login("giahuy","123");
        var chay1 = new Thread(() -> user1.placeBid(1));
        var chay2 = new Thread(() -> user2.placeBid(1));
        chay1.start();
        chay2.start();

        // user1.showRole();
        // Bidder newUser = (Bidder) user1.changeRole(Role.BIDDER);
        // newUser.placeBid(1);
        // newUser.checkNotifications();
        // Seller newUser = (Seller) user.changeRole(Role.SELLER);

        
        // Arts draft = newUser.createItem(Arts.Builder.class,"Kaguya","phim cosmic princess kaguya")
        //     .withArtist("duong")
        //     .withyearOfCreation(2025)
        //     .build();
        // newUser.uploadItem(draft,1000,10, 2);

        // Electronics draft1 = newUser.createItem(Electronics.Builder.class, "Samsung", "nokia 2043")
        //                             .withBrand("Xiami")
        //                             .withColor("Red")
        //                             .build();
        // newUser.uploadItem(draft1,1000,10,3);

        // Vehicle draft2 = newUser.createItem(Vehicle.Builder.class, "Honda", "đây là xe jupiter")
        //                         .withBrand("sámung")
        //                         .build();
        // newUser.uploadItem(draft2,200,12,4);
    } 
}
