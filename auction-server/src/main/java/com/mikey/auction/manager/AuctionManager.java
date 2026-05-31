package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mikey.auction.auction.Auction;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.database.NotificationDAO;
import com.mikey.auction.database.UserDAO;
import com.mikey.auction.dto.AuctionInfo;
import com.mikey.auction.dto.AutoBidInfo;
import com.mikey.auction.items.Item;
import com.mikey.auction.socket.AuctionServer;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;


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



    public AuctionInfo placeBid(Bidder bidder, AuctionInfo auctionInfo, double oldPrice) {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        try (Connection connection = auctionDAO.getConnect()) {
            connection.setAutoCommit(false);

            // 1. Thực hiện cập nhật số tiền và lịch sử giao dịch vào MySQL (Tầng DAO lo)
            auctionDAO.updateAuction(connection, auctionInfo, bidder.getId(), oldPrice);
            auctionDAO.updateTransaction(connection, auctionInfo, bidder.getId());
            connection.commit(); // Chốt hạ giao dịch thủ công thành công!

            // 2. KÍCH HOẠT AUTO BIDDING: Kéo các đại gia cài tự động vào đọ tiền
            auctionDAO.triggerAutoBids(auctionInfo.getId());

            // 3. KÉO DỮ LIỆU MỚI NHẤT: Lấy mốc giá cuối cùng sau khi đọ Auto-Bid xong xuôi
            AuctionInfo freshAuctionInfo = auctionDAO.searchAuctionById(auctionInfo.getId());

            if (freshAuctionInfo != null) {

                NotificationManager.getInstance().notiAll(freshAuctionInfo, bidder);

                com.google.gson.Gson broadcastGson = new com.google.gson.GsonBuilder()
                        .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, t, ctx) -> new com.google.gson.JsonPrimitive(src.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                        .registerTypeAdapter(java.time.LocalDateTime.class, (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, t, ctx) -> java.time.LocalDateTime.parse(json.getAsString(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .create();

                // Đồng bộ giá cho các máy khác đang mở xem sảnh/chi tiết
                AuctionServer.broadcast("AUCTION|UPDATE_PRICE|" + broadcastGson.toJson(freshAuctionInfo));

                System.out.println("Manager: Đã xử lý khép quy trình đặt giá thành công cho Phiên ID: " + freshAuctionInfo.getId());
                return freshAuctionInfo;
            }

        } catch (java.sql.SQLException e) {
            System.err.println("Lỗi nghiệp vụ đặt giá tại tầng Manager: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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

//  ĐÃ SỬA: Đổi kiểu trả về thành AuctionInfo để trả nguyên cục dữ liệu chứa giá mới cho Client
    public AuctionInfo registerAutoBid(AutoBidInfo info) {
        //Lưu mức giá tối đa mà người dùng muốn đặt, cùng với ID phiên đấu và ID người dùng
        boolean success = AuctionDAO.getInstance().registerAutoBid(info);
        if (success) {
            AuctionDAO.getInstance().triggerAutoBids(info.getAuctionId());
            // lấy lên bản ghi mới nhất sau khi đã kích hoạt auto bid để trả về cho máy vừa cài đặt
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
