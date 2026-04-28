package Auction;
import Database.*;
import Items.*;
import JustToDisplayInfo.AuctionInfo;
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
        
        try (Connection connection = AuctionDAO.getInstance().getConnect()) { 
            connection.setAutoCommit(false);
        
            if (item.upload(connection, price, stepPrice, durations)) {
                connection.commit();
            } else {
                connection.rollback();
                throw new SQLException("Upload failed");
            }

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
            connection.commit();
            return true;

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return false;

    }
    public Auction findAuction(int id){
        try(Connection connection = AuctionDAO.getInstance().getConnect()) {
            return AuctionDAO.getInstance().findById(connection, id);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<AuctionInfo> auctionList(){
        return AuctionDAO.getInstance().getAllAuctions();
    }
    
}
