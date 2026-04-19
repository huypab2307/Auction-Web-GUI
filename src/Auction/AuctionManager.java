package Auction;
import Database.ArtsDAO;
import Items.*;
import User.*;

public class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getAuction(){
        return instance;
    }
    public void uploadItem(Arts art, User seller) {
        if (seller.getRole() != Role.SELLER) {
            System.out.println("Lỗi: Người dùng không có quyền bán!");
            return;
        }

        if (art.getPrice() <= 0) {
            System.out.println("Lỗi: Giá phải lớn hơn 0!");
            return;
        }
        ArtsDAO.getItemDAO().createItem(art);
        System.out.println("Manager: Đã đăng bán món hàng " + art.getName());
    }
}
