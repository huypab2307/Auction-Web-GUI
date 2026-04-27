package Auction;
import Database.*;
import Items.*;
import User.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getInstance(){
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

    public boolean placebid(Bidder bidder, int id, double oldPrice){
        AuctionDAO auctionDAO = AuctionDAO.getInstance();

        try(Connection connection = auctionDAO.getConnect()){
            connection.setAutoCommit(false);
            Auction auction = auctionDAO.findById(connection, id);
            auctionDAO.updateAuction(connection, auction, bidder.getId(),oldPrice);
            auctionDAO.updateTransaction(connection, auction, bidder.getId());

            Notifications notification = NotificationManager.getInstance().subscribeAuction(connection, auction, bidder);
            if (notification == null) throw new SQLException("Có lỗi xảy ra khi thống báo");
            connection.commit();
            return true;

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }
    public void findAuction(int id){
        try(Connection connection = AuctionDAO.getInstance().getConnect()) {
            Auction auction = AuctionDAO.getInstance().findById(connection, id);
            System.out.println("giá hiện tại của vật phẩm: " + auction.getCurPrice());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<AuctionInfo> auctionList(){
        return AuctionDAO.getInstance().getAllAuctions();
    }
}
