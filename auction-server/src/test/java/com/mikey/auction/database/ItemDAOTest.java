package com.mikey.auction.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mikey.auction.items.Electronics;

public class ItemDAOTest {

    @Test
    public void testItemDAOSingleton() {
        // Kiểm tra tính chất duy nhất của Singleton trên RAM (Hoàn toàn an toàn)
        ItemDAO dao1 = ElectronicsDAO.getInstance();
        ItemDAO dao2 = ElectronicsDAO.getInstance();
        
        assertSame(dao1, dao2, "ElectronicsDAO phải là một instance duy nhất (Singleton)");
    }

    @Test
    public void testInsertAndGetItem_SafelyWithMock() {
        assertDoesNotThrow(() -> {
            ItemDAO itemDAO = ElectronicsDAO.getInstance();
            
            // 1. TẠO RA CÁC ĐỐI TƯỢNG GIẢ CỦA DATABASE (Thay vì gọi getConnect)
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockStmt = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            
            // 2. DẠY ĐỒ GIẢ CÁCH PHẢN HỒI (Giả lập TiDB Cloud)
            when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1); // Báo cáo: "Đã insert thành công 1 dòng"
            when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt(1)).thenReturn(999); // Trả về ID 999 cho chiếc iPhone 15 vừa tạo
            
            // 3. TRUYỀN CONNECTION GIẢ VÀO DAO ĐỂ THỰC THI THUẬT TOÁN
            Electronics item = new Electronics("iPhone 15", "Màu đen, 128GB", com.mikey.auction.items.ItemType.ELECTRONICS, 1, 1, "Apple");
            itemDAO.insertBaseItem(mockConn, item, "ELECTRONICS", "path/to/image.png");
            
            // 4. KIỂM CHỨNG BẰNG BẰNG CHỨNG THÉP (Verify)
            // Kiểm tra xem ItemDAO có thực sự gọi lệnh executeUpdate() để lưu data không?
            verify(mockStmt, times(1)).executeUpdate();
            
        }, "Hàm insertBaseItem phải chạy logic gán tham số mượt mà mà không ném lỗi");
    }
}