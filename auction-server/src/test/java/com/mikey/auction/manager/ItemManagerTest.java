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
        
        // 1. Dữ liệu chữ thông thường
        data.put("name", "BMW 3 Series");
        data.put("description", "Xe lướt");
        data.put("type", "VEHICLE");
        data.put("imagePath", "path/to/img.png");
        data.put("brand", "BMW");
        data.put("model", "3 Series");
        data.put("trim", "Sport");
        data.put("titleStatus", "Clean");

        // 2. DỮ LIỆU SỐ BẮT BUỘC ĐỂ KHÔNG BỊ LỖI ÉP KIỂU
        data.put("sellerId", "1");      // Dành cho preProcessing
        data.put("mileage", "15000.5"); // Dành cho setVehicle (Double)
        data.put("mFG", "2020");        // Dành cho setVehicle (Integer)

        assertDoesNotThrow(() -> {
            Item item = ItemManager.getInstance().preProcessing(data);
            if (item != null) {
                assertEquals(ItemType.VEHICLE, item.getType());
                assertInstanceOf(Vehicle.class, item);
            }
        });
    }

    @Test
    public void testFindItemById() throws Exception {
        try (MockedStatic<ElectronicsDAO> mockedDao = mockStatic(ElectronicsDAO.class)) {
            ElectronicsDAO mockDaoInstance = mock(ElectronicsDAO.class);
            Connection mockConn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            
            mockedDao.when(ElectronicsDAO::getInstance).thenReturn(mockDaoInstance);
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            
            when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true).thenReturn(false);

            assertDoesNotThrow(() -> {
                ItemManager.getInstance().findItemById(ItemType.ELECTRONICS, 1);
            });
        }
    }

    @Test
    public void testSetElectronics() {
        Item item = new Electronics("Phone", "Desc", ItemType.ELECTRONICS, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        
        data.put("brand", "Samsung");
        data.put("status", "New");
        data.put("color", "Black");
        
        // DỮ LIỆU SỐ BẮT BUỘC TRONG setElectronics ĐỂ CHỐNG LỖI
        data.put("power", "15");       // Integer
        data.put("voltage", "5.0");    // Double
        data.put("current", "3.0");    // Double
        data.put("weight", "0.2");     // Double

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setElectronics(item, data);
        });
    }
}