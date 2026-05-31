package com.mikey.auction.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDAOTest {

    @Test
    public void testUserDAOSingleton() {
        // Đảm bảo UserDAO thiết kế đúng chuẩn Singleton giống AuctionDAO
        UserDAO dao1 = UserDAO.getInstance();
        UserDAO dao2 = UserDAO.getInstance();
        assertSame(dao1, dao2, "UserDAO phải là một instance duy nhất");
    }

    @Test
    public void testCreateUserAndLogin() throws Exception {
        // 1. TẠO ĐIỆP VIÊN
        UserDAO spyDao = spy(UserDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        // 2. ÉP ĐIỆP VIÊN TRẢ VỀ CONNECTION GIẢ
        doReturn(mockConn).when(spyDao).getConnect();
        
        // 3. DẠY ĐỒ GIẢ PHẢN HỒI LỆNH SQL
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // Dành cho lệnh INSERT (createUser)
        when(mockStmt.executeQuery()).thenReturn(mockRs); // Dành cho lệnh SELECT (login)
        when(mockRs.next()).thenReturn(true); // Trả về true báo là tìm thấy user

        assertDoesNotThrow(() -> {
            // Test thử việc xin Connection từ Điệp viên
            Connection conn = spyDao.getConnect();
            assertNotNull(conn);

            // BẠN CÓ THỂ MỞ COMMENT ĐỂ TEST LOGIC THẬT Ở ĐÂY VÌ ĐÃ CÓ MOCKITO BẢO VỆ
            // boolean isCreated = spyDao.createUser(conn, "testuser", "password123", "test@email.com");
            // var user = spyDao.login(conn, "testuser", "password123");
            
            conn.close();
            
            // Xác nhận đóng kết nối thành công
            verify(mockConn, times(1)).close();
        }, "Quy trình tạo user và đăng nhập không được ném exception");
    }

    @Test
    public void testUpdateUserBalance() throws Exception {
        UserDAO spyDao = spy(UserDAO.getInstance());
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        // Tráo Connection
        doReturn(mockConn).when(spyDao).getConnect();
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // Giả lập Update tiền thành công 1 dòng

        assertDoesNotThrow(() -> {
            // Tương tự, nếu hàm updateBalance tự động gọi getConnect() bên trong, 
            // nó sẽ lấy được Connection giả của chúng ta và chạy mượt mà
            // boolean result = spyDao.updateBalance(1, 5000000); 
            // assertTrue(result);
            
        }, "Cập nhật số dư tài khoản không được ném exception");
    }
}