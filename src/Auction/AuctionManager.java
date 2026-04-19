package Auction;
import Database.ArtsDAO;
import Database.ElectronicsDAO;
import Database.VehicleDAO;
import Items.*;
import User.*;

public class AuctionManager {
    private static final AuctionManager instance = new AuctionManager();

    public static AuctionManager getAuction(){
        return instance;
    }
    public void uploadItem(Item item, User seller) {
        if (seller.getRole() != Role.SELLER) {
            System.out.println("Lỗi: Người dùng không có quyền bán!");
            return;
        }

        if (item.getPrice() <= 0) {
            System.out.println("Lỗi: Giá phải lớn hơn 0!");
            return;
        }
        ItemType type = item.getType();
        switch (type) {
            case ARTS:
                ArtsDAO.getInstance().createItem((Arts) item);
                break;
            case ELECTRONICS:
                ElectronicsDAO.getInstance().createItem((Electronics) item);
                break;
            case VEHICLE:
                VehicleDAO.getInstance().createItem((Vehicle) item);
                break;
            default:
                System.out.println("Lỗi: Loại sản phẩm không hỗ trợ!");
                return;
        }
        System.out.println("Manager: Đã đăng bán món hàng " + item.getName());
    }
}
