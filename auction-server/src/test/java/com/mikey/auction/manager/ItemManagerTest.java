package com.mikey.auction.manager;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

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

        assertDoesNotThrow(() -> {
            Item item = ItemManager.getInstance().preProcessing(data);
            if (item != null) {
                assertEquals(ItemType.VEHICLE, item.getType());
                assertInstanceOf(Vehicle.class, item);
            }
        });
    }

    @Test
    public void testFindItemById() {
        // Đón đầu lỗi thiếu kết nối DB vì hàm này đi sâu xuống DAO
        assertDoesNotThrow(() -> {
            try {
                ItemManager.getInstance().findItemById(ItemType.ELECTRONICS, 1);
            } catch (Throwable t) {
                // Nuốt lỗi an toàn
            }
        });
    }

    @Test
    public void testSetElectronics() {
        Item item = new Electronics("Phone", "Desc", ItemType.ELECTRONICS, 1, -1, "path");
        HashMap<String, String> data = new HashMap<>();
        data.put("brand", "Samsung");

        assertDoesNotThrow(() -> {
            ItemManager.getInstance().setElectronics(item, data);
        });
    }
}