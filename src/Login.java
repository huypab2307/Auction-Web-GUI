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

        
        Arts draft = newUser.createItem(Arts.Builder.class,"Kaguya","phim cosmic princess kaguya")
            .withArtist("duong")
            .withyearOfCreation(2025)
            .build();
        newUser.uploadItem(draft,1000,10, 2);

        Electronics draft1 = newUser.createItem(Electronics.Builder.class, "Samsung", "nokia 2043")
                                    .withBrand("Xiami")
                                    .withColor("Red")
                                    .build();
        newUser.uploadItem(draft1,1000,10,3);

        Vehicle draft2 = newUser.createItem(Vehicle.Builder.class, "Honda", "đây là xe jupiter")
                                .withBrand("sámung")
                                .build();
        newUser.uploadItem(draft2,200,12,4);
    } 
}
