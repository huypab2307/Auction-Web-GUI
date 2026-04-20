package Auction;
import Database.ArtsDAO;
import Database.AuctionDAO;
import Database.ElectronicsDAO;
import Database.VehicleDAO;
import Items.*;
import User.*;
import java.sql.Connection;
import java.sql.SQLException;

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
                AuctionDAO.getInstance().createAuction(connection, id, price, stepPrice, durations);
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
        try{
            AuctionDAO auctions = AuctionDAO.getInstance();
            Auction auction = auctions.findById(id);
            auctions.updateAuction(auction, bidder.getId());
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
