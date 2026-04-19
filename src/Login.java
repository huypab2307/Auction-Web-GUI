import Auction.*;
import Database.UserDAO;
import Items.*;
import User.*;
class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getAuction();
        UserDAO users = UserDAO.getUserDAO();
        // u.register("DuongDuong","jennyhuyn"); 
        // u.register("Jenny","Matkhau");
        User user = users.login("Jenny","Matkhau");
        user.showRole();
        Seller newUser = (Seller) user.changeRole(Role.SELLER);
        Arts draft = newUser.sellItem(Arts.Builder.class,"Kaguya","phim cosmic princess kaguya",100)
            .withArtist("duong")
            .withyearOfCreation(2025)
            .build();
        auction.uploadItem(draft, newUser);
    } 
}
