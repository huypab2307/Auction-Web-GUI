import Auction.*;
import Database.NotificationDAO;
import Database.UserDAO;
import User.*;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Login {

    public static void startNotificationTask(int userId) {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Runnable checkNotiTask = () -> {
        try (Connection conn = Database.AuctionDAO.getInstance().getConnect()) {
            List<Notifications> list = NotificationDAO.getInstance().checkNotifications(conn, userId, false);
            
            if (!list.isEmpty()) {
                System.out.println("\n--- [Hòm thư mới] ---");
                for (Notifications n : list) {
                    n.readNotification();
                    AuctionManager.getAuction().findAuction(n.getAuctionId());
                }
                
                NotificationDAO.getInstance().markAllAsRead(conn, userId);
            }
        } catch (Exception e) {
            System.err.println("Lỗi luồng thông báo: " + e.getMessage());
        }
    };

    scheduler.scheduleAtFixedRate(checkNotiTask, 0, 30, TimeUnit.SECONDS);
}
    public static void main(String[] args) {
        AuctionManager auction = AuctionManager.getAuction();
        UserDAO users = UserDAO.getInstance();

        // users.register("togedemaru","Matkhau");
        // users.register("Jenny", "Matkhau");
        
        User user = users.login("jenny","Matkhau");
        Bidder newUser = (Bidder) user.changeRole(Role.BIDDER);
        startNotificationTask(user.getId());
        String name = user.getUsername();
        try(Scanner sc = new Scanner(System.in)){
            while (true){
                String command = sc.next();
                if (command.equals("BID")){
                    newUser.placeBid(1);
                }
            }
        }
        // user.showRole();
        // Bidder newUser = (Bidder) user.changeRole(Role.BIDDER);
        // newUser.placeBid(1);
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
