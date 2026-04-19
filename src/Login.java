import Auction.*;
import Database.UserDAO;
import Items.*;
import User.*;
class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getAuction();
        UserDAO users = UserDAO.getInstance();
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

        Electronics draft1 = newUser.sellItem(Electronics.Builder.class, "Samsung", "nokia 2043", 1000)
                                    .withBrand("Xiami")
                                    .withColor("Red")
                                    .build();
        auction.uploadItem(draft1, newUser);

        Vehicle draft2 = newUser.sellItem(Vehicle.Builder.class, "Honda", "đây là xe jupiter", 2000)
                                .withBrand("sámung")
                                .build();
        auction.uploadItem(draft2, newUser);
    } 
}
