import Auction.*;
import Database.UserDAO;
import User.*;

class Login {
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getInstance();
        UserDAO users = UserDAO.getInstance();

        // users.register("togedemaru","Matkhau");
        // users.register("jenny", "Matkhau");
                
        Bidder user1 = (Bidder) users.login("hhh", "ccc"); // Xóa khoảng trắng thừa đi nhé
        Bidder user2 = (Bidder) users.login("giahuy", "123");

        // Kiểm tra xem có ông nào bị null không trước khi chạy thread
        if (user1 == null || user2 == null) {
            System.out.println("Lỗi: Tài khoản không tồn tại, kiểm tra lại username/password đi Dương ơi!");
            return;
        }

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
