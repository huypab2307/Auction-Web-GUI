package com.mikey.auction.manager;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.user.Seller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;


public class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getInstance(){
        return instance;
    }
   public void uploadItem(Item item, double price, double stepPrice, LocalDateTime startTime, LocalDateTime endTime) {

       if (price <= 0 || stepPrice <= 0 || stepPrice > price) {
           System.out.println("Lỗi: Giá khởi điểm và bước giá không hợp lệ!");
           return;
       }

       try (Connection connection = AuctionDAO.getInstance().getConnect()) {
           connection.setAutoCommit(false);
           Item createdItem = ItemManager.getInstance().uploadItem(connection, item);

           if (AuctionDAO.getInstance().createAuction(connection, createdItem.getId(), createdItem.getSellerId(), price, stepPrice, startTime, endTime)) {
               connection.commit();
           } else {
               connection.rollback();
               throw new SQLException("Upload failed");
           }

       }catch(SQLException e){
           System.out.println(e.getMessage());
       }
   }

    public boolean placeBid(Bidder bidder, int id, double oldPrice){
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
