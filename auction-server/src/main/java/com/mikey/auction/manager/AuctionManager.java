package com.mikey.auction.manager;

import com.mikey.auction.auction.Auction;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.NotificationDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;


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


// 👉 ĐÃ SỬA: Đổi kiểu trả về từ boolean sang AuctionInfo để lấy giá mới lập tức
    public AuctionInfo placeBid(Bidder bidder, AuctionInfo auctionInfo, double oldPrice) {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        try (Connection connection = auctionDAO.getConnect()) {
            connection.setAutoCommit(false);
            auctionDAO.updateAuction(connection, auctionInfo, bidder.getId(), oldPrice);
            auctionDAO.updateTransaction(connection, auctionInfo, bidder.getId());
            
            connection.commit(); // Chốt đơn cho người đặt giá thủ công

            // KÍCH HOẠT AUTO BIDDING: Máy tự động đọ giá (nếu có)
            auctionDAO.triggerAutoBids(auctionInfo.getId());

            // LẤY DỮ LIỆU MỚI NHẤT: Bao gồm cả giá sau khi Auto-Bid đọ nhau xong
            AuctionInfo freshAuctionInfo = auctionDAO.searchAuctionById(auctionInfo.getId());

            // Bắn thông báo qua cổng phụ cho các máy KHÁC đang cùng xem
            NotificationManager.getInstance().notiAll(freshAuctionInfo, bidder);
            
            // 👉 QUAN TRỌNG: Trả về đối tượng mới tinh cho CHÍNH MÁY vừa bấm nút
            return freshAuctionInfo;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Thất bại trả về null
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

    public ArrayList<AuctionInfo> getFollowedAuctions(int userId){
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        ArrayList<AuctionInfo> auctionList = new ArrayList<>();
        try(Connection connection = auctionDAO.getConnect()) {
            ArrayList<Integer> auctionIdList = NotificationDAO.getInstance().findSubscribedAuctions(connection, userId);
            for (int id : auctionIdList) {
                auctionList.add(auctionDAO.searchAuctionById(id));
            }
            return auctionList;

        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateAuction(AuctionInfo info) {
        return AuctionDAO.getInstance().updateAuction(info);
    }

// 👉 ĐÃ SỬA: Đổi kiểu trả về thành AuctionInfo để trả nguyên cục dữ liệu chứa giá mới cho Client
    public AuctionInfo registerAutoBid(com.mikey.auction.dto.AutoBidInfo info) {
        boolean success = AuctionDAO.getInstance().registerAutoBid(info);
        if (success) {
            AuctionDAO.getInstance().triggerAutoBids(info.getAuctionId());
            AuctionInfo freshInfo = AuctionDAO.getInstance().searchAuctionById(info.getAuctionId());
            if (freshInfo != null) {
                try {
                    Bidder bidder = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, UserDAO.getInstance().findById(info.getUserId()));
                    NotificationManager.getInstance().notiAll(freshInfo, bidder);
                } catch (Exception e) {}
                
                return freshInfo; // TRẢ VỀ CHO MÁY VỪA CÀI AUTO BID BIẾT ĐỂ NHẢY SỐ
            }
        }
        return null;
    }
}
