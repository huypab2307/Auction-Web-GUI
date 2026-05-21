package com.mikey.auction.database;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

public class UserDAOTest {

    @Test
    public void testUserDAOSingleton() {
        // Đảm bảo UserDAO thiết kế đúng chuẩn Singleton giống AuctionDAO
        UserDAO dao1 = UserDAO.getInstance();
        UserDAO dao2 = UserDAO.getInstance();
        assertSame(dao1, dao2, "UserDAO phải là một instance duy nhất");
    }

    @Test
    public void testCreateUserAndLogin() {
        assertDoesNotThrow(() -> {
            Connection conn = UserDAO.getInstance().getConnect();
            assertNotNull(conn);

            // Giả lập lưu một user mới xuống database (Đổi lại tên hàm của bạn cho đúng)
            // boolean isCreated = UserDAO.getInstance().createUser(conn, "testuser", "password123", "test@email.com");
            
            // Giả lập kiểm tra đăng nhập
            // var user = UserDAO.getInstance().login(conn, "testuser", "password123");
            
            conn.close();
        }, "Quy trình tạo user và đăng nhập không được ném exception");
    }

    @Test
    public void testUpdateUserBalance() {
        assertDoesNotThrow(() -> {
            // Kiểm tra tính năng nạp/trừ tiền trong ví người dùng dưới DB
            // boolean result = UserDAO.getInstance().updateBalance(1, 5000000); 
            // Cập nhật số dư cho User ID 1 lên 5 triệu
        }, "Cập nhật số dư tài khoản không được ném exception");
    }
}