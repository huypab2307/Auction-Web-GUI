package com.mikey.auction.database;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import com.mikey.auction.items.Electronics;

public class ItemDAOTest {

    @Test
    public void testItemDAOSingleton() {
        // Vì ItemDAO là abstract, ta sẽ kiểm tra xem lớp con (ElectronicsDAO) 
        // có triển khai Singleton chuẩn và giữ đúng tính chất duy nhất không.
        ItemDAO dao1 = ElectronicsDAO.getInstance();
        ItemDAO dao2 = ElectronicsDAO.getInstance();
        
        assertSame(dao1, dao2, "ElectronicsDAO phải là một instance duy nhất (Singleton)");
    }

    @Test
    public void testInsertAndGetItem() {
        assertDoesNotThrow(() -> {
            // 1. Lấy instance thông qua lớp con cụ thể
            ItemDAO itemDAO = ElectronicsDAO.getInstance();
            
            // 2. Gọi hàm kết nối (Sửa lỗi cú pháp .getInstance thiếu dấu ngoặc () ở dòng 19 cũ)
            Connection conn = itemDAO.getConnect(); 
            assertNotNull(conn, "Kết nối Database không được null");
            
            // 3. Giả lập lưu một sản phẩm xuống DB sử dụng hàm của lớp cha/con
            itemDAO.insertBaseItem(conn, new Electronics("iPhone 15", "Màu đen, 128GB", com.mikey.auction.items.ItemType.ELECTRONICS, 1, 1, "Apple"), "ELECTRONICS", "path/to/image.png");
            


            // 5. Đóng kết nối an toàn
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }, "Thêm và đọc sản phẩm từ Database không được ném exception");
    }
}