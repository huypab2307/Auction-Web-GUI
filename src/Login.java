import Auction.*;
import Database.UserDAO;
import Items.*;
import User.*;

class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getInstance();
        UserDAO users = UserDAO.getInstance();

        // users.register("doraemon","nobita");
        // users.register("jenny", "Matkhau");
                
        Bidder user = (Bidder) users.login("duong", "1"); 
        // user.checkNotifications();
        // user1.showRole();
        // Bidder newUser = (Bidder) user1.changeRole(Role.BIDDER);
        // newUser.placeBid(1);
        // newUser.checkNotifications();
        // Seller newUser = (Seller) user.changeRole(Role.SELLER);

        
        // Arts draft = newUser.createItem(Arts.Builder.class,"The Screaming","doraemo","/JavaGui/doraemon.jpg")
        //     .withArtist("duong")
        //     .withyearOfCreation(2025)
        //     .build();
        // newUser.uploadItem(draft,7000,1230, 5);

        // Electronics draft1 = newUser.createItem(Electronics.Builder.class, "Samsung", "nokia 2043")
        //                             .withBrand("Xiami")
        //                             .withColor("Red")
        //                             .build();
        // newUser.uploadItem(draft1,1000,10,3);

        // Vehicle draft2 = newUser.createItem(Vehicle.Builder.class, "xe tương lai", "đây là xe tương lai","/JavaGui/testlo.jpg")
        //                         .withBrand("tesla")
        //                         .withMileage(1000)
        //                         .build();
        // newUser.uploadItem(draft2,50000000,100000,5);
    } 
}
