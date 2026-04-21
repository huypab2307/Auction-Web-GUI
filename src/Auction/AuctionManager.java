package Auction;
import Database.*;
import Items.*;
import User.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getAuction(){
        return instance;
    }
    public void uploadItem(Item item,double price,double stepPrice,int durations, Seller seller) {
        if (seller.getRole() != Role.SELLER) {
            System.out.println("Lỗi: Người dùng không có quyền bán!");
            return;
        }
        if (price <= 0 || stepPrice <= 0 || stepPrice > price) {
            System.out.println("Lỗi: Giá khởi điểm và bước giá không hợp lệ!");
            return;
        }
        try(Connection connection = AuctionDAO.getInstance().getConnect()){
            connection.setAutoCommit(false);
            ItemType type = item.getType();
            int id = -1;
            switch (type) {
                case ARTS:
                    id = ArtsDAO.getInstance().createItem(connection, (Arts) item);
                    break;
                case ELECTRONICS:
                    id = ElectronicsDAO.getInstance().createItem(connection,(Electronics) item);
                    break;
                case VEHICLE:
                    id = VehicleDAO.getInstance().createItem(connection, (Vehicle) item);
                    break;
                default:
                    System.out.println("Lỗi: Loại sản phẩm không hỗ trợ!");
                    return;
            }

            if (id > 0){
                System.out.println("Manager: Đã đăng bán món hàng " + item.getName());
                AuctionDAO.getInstance().createAuction(connection, id,seller.getId(), price, stepPrice, durations);
                connection.commit();
                return;
            }   
            connection.rollback();
            throw new SQLException("có lỗi xảy ra trong khi upload sản phẩm");
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void placebid(Bidder bidder, int id){
        try(Connection connection = AuctionDAO.getInstance().getConnect()){
            connection.setAutoCommit(false);
            AuctionDAO auctions = AuctionDAO.getInstance();
            NotificationDAO notificaions = NotificationDAO.getInstance();
            Auction auction = auctions.findById(connection, id);
            auctions.updateAuction(connection, auction, bidder.getId());
            auctions.updateTransaction(connection, auction, bidder.getId());
            Notifications notification = notificaions.subscribeAuction(connection, auction, bidder);
            ArrayList<Integer> log = notificaions.findNotificationList(connection, auction.getId());
            notificaions.notiAll(connection, notification, log);
            connection.commit();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void readNotification(User user){
        NotificationDAO notification = NotificationDAO.getInstance();
        try(Connection connection = notification.getConnect()){
            connection.setAutoCommit(false);
            ArrayList<Notifications> notifications = notification.checkNotifications(connection, user.getId(), false);
            notifications.forEach(s -> s.readNotification());
            notification.markAllAsRead(connection, user.getId());
            connection.commit();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public void findAuction(int id){
        try(Connection connection = AuctionDAO.getInstance().getConnect()) {
            Auction auction = AuctionDAO.getInstance().findById(connection, id);
            System.out.println("giá hiện tại của vật phẩm: " + auction.getCurPrice());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
