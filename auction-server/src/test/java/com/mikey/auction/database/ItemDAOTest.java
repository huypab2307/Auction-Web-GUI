package com.mikey.auction.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;

public class ItemDAOTest {

    @Test
    public void testItemDAOSingleton() {
        ItemDAO dao1 = ItemDAO.getInstance();
        ItemDAO dao2 = ItemDAO.getInstance();
        assertSame(dao1, dao2, "ItemDAO phải là một instance duy nhất");
    }

    @Test
    public void testInsertAndGetItem() {
        assertDoesNotThrow(() -> {
            Connection conn = ItemDAO.getInstance().getConnect();
            
            // Giả lập lưu một sản phẩm (ví dụ: đồ điện tử) xuống DB
            ItemDAO.getInstance().insertItem(conn, "iPhone 15", "Màu đen, 128GB", "ELECTRONICS");
            
            // Giả lập lấy danh sách hoặc tìm kiếm món đồ vừa lưu
            var items = ItemDAO.getInstance().getAllItems(conn);
            assertNotNull(items);

            conn.close();
        }, "Thêm và đọc sản phẩm từ Database không được ném exception");
    }
}