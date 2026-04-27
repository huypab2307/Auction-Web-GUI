import Auction.*;
import Database.UserDAO;
import Items.*;
import User.*;

class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getInstance();
        UserDAO users = UserDAO.getInstance();

        // users.register("togedemaru","Matkhau");
        // users.register("jenny", "Matkhau");
                
        Bidder user = (Bidder) users.login("hhh", "ccc"); 

        // user1.showRole();
        // Bidder newUser = (Bidder) user1.changeRole(Role.BIDDER);
        // newUser.placeBid(1);
        // newUser.checkNotifications();
        Seller newUser = (Seller) user.changeRole(Role.SELLER);

        
        Arts draft = newUser.createItem(Arts.Builder.class,"Kaguya","phim cosmic princess kaguya")
            .withArtist("duong")
            .withyearOfCreation(2025)
            .build();
        newUser.uploadItem(draft,1000,10, 2);

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
