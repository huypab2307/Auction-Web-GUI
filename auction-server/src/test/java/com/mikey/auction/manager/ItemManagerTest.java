package com.mikey.auction.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.mikey.auction.database.ElectronicsDAO;
import com.mikey.auction.items.Electronics;
import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;
import com.mikey.auction.items.Vehicle;

public class ItemManagerTest {

    @Test
    public void testCreateItemArts() {
        assertDoesNotThrow(() -> {
            Item item = ItemManager.getInstance().createItem(ItemType.ARTS, "Mona Lisa", "Painting", 1, "path");
            assertEquals(ItemType.ARTS, item.getType());
            assertEquals("Mona Lisa", item.getName());
        });
    }

    @Test
    public void testPreprocessingVehicle() {
        HashMap<String, String> data = new HashMap<>();
        data.put("name", "BMW 3 Series");
        data.put("type", "VEHICLE");
        data.put("brand", "BMW");
        
        // VÁ LỖI NUMBER FORMAT EXCEPTION: 
        // Cấp cứu dữ liệu! Phải truyền đủ các thông số là "số" dưới dạng chuỗi để code thật ép kiểu (parseInt) không bị nổ.
        data.put("ownerId", "1");
        data.put("year", "2022");
        data.put("mileage", "10000");
        data.put("seats", "4");
        data.put("doors", "4");
        data.put("condition", "1");

        assertDoesNotThrow(() -> {
            Item item = ItemManager.getInstance().preProcessing(data);
            if (item != null) {
                assertEquals(ItemType.VEHICLE, item.getType());
                assertInstanceOf(Vehicle.class, item);
            }
        });
    }

    @Test
    public void testFindItemById() throws Exception { // THÊM THROWS EXCEPTION
        // VÁ LỖI NUỐT EXCEPTION: Dùng Mockito chặn điệp viên để gọi DB an toàn
        // (Giả sử hệ thống gọi xuống ElectronicsDAO để tìm đồ điện tử)
        try (MockedStatic<ElectronicsDAO> mockedDao = mockStatic(ElectronicsDAO.class)) {
            ElectronicsDAO mockDaoInstance = mock(ElectronicsDAO.class);
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class); // TẠO MA-NƠ-CANH RESULT SET
            
            mockedDao.when(ElectronicsDAO::getInstance).thenReturn(mockDaoInstance);
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            
            // Dạy Mockito cách nôn ra ResultSet ảo
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true).thenReturn(false); // Có 1 dòng dữ liệu rồi ngắt

            assertDoesNotThrow(() -> {
                // Chạy hàm thật, nó sẽ lấy đúng Connection giả và ResultSet giả ở trên
                ItemManager.getInstance().findItemById(ItemType.ELECTRONICS, 1);
            }, "Tìm kiếm Item không được ném lỗi crash (NullPointer) khi chạy qua Mockito");
        }
    }

    @Test
    public void testSetElectronics() {
        Item item = new Electronics("Phone", "Desc", ItemType.ELECTRONICS, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        data.put("brand", "Samsung");
        
        // VÁ LỖI NUMBER FORMAT EXCEPTION: Tương tự như Vehicle, thêm đủ thông số của đồ điện tử
        data.put("ownerId", "1");
        data.put("warranty", "12"); // 12 tháng
        data.put("ram", "8"); 
        data.put("storage", "256");
        data.put("batteryCapacity", "4500");
        data.put("condition", "1");

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setElectronics(item, data);
        });
    }
}