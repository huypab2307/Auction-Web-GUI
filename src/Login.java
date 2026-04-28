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
                
        Bidder user = (Bidder) users.login("jenny", "Matkhau"); 
        // user.checkNotifications();
        // user1.showRole();
        // Bidder newUser = (Bidder) user1.changeRole(Role.BIDDER);
        // newUser.placeBid(1);
        // newUser.checkNotifications();
        Seller newUser = (Seller) user.changeRole(Role.SELLER);

        
        Arts draft = (Arts) newUser.createItem(ItemType.ARTS,"The Screaming","doraemo","/JavaGui/doraemon.jpg");
        draft.setArts(null, 2000, "700", null);
        newUser.uploadItem(draft,7000,1230, 5);

    } 
}
