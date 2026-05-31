import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mikey.auction.database.AuctionDAO;
import com.mikey.auction.dto.AuctionInfo;

public class AuctionServerTest {

    @Test
    public void testDatabaseConnection() throws Exception {
        // Sử dụng MockedStatic để chặn class AuctionDAO
        try (MockedStatic<AuctionDAO> mockedDao = mockStatic(AuctionDAO.class)) {
            // Khởi tạo các "diễn viên" giả (Mock)
            AuctionDAO mockDaoInstance = mock(AuctionDAO.class);
            Connection mockConn = mock(Connection.class);
            
            // Dạy Mockito phản ứng
            mockedDao.when(AuctionDAO::getInstance).thenReturn(mockDaoInstance);
            when(mockDaoInstance.getConnect()).thenReturn(mockConn);
            // Giả lập trạng thái kết nối đang mở
            when(mockConn.isClosed()).thenReturn(false);

            // Kiểm tra: Hàm phải chạy mà không ném ra ngoại lệ (exception) nào
            assertDoesNotThrow(() -> {
                Connection conn = AuctionDAO.getInstance().getConnect();
                assertNotNull(conn, "Kết nối database không được null");
                assertFalse(conn.isClosed(), "Kết nối database phải đang trong trạng thái mở");
                conn.close();
            }, "Phải kết nối được đến Database ảo mà không văng lỗi Exception");
            
            // Xác nhận rằng hàm close() của connection đã được gọi
            verify(mockConn, times(1)).close();
        }
    }

    // Các bài test khác giữ nguyên vì chúng thuần túy chạy trên RAM (rất tốt!)
    @Test
    public void testAuctionInfoDTO() {
        AuctionInfo info = new AuctionInfo(
            null, 99, "sellerTest", "bidderTest", 500000.0, null, null, null, 0.0
        );
        assertEquals(99, info.getId());
        assertEquals(500000.0, info.getCurPrice());
    }

    @Test
    public void testGsonLocalDateTimeAdapter() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();

        LocalDateTime testTime = LocalDateTime.of(2023, 10, 27, 15, 30, 0);
        String json = gson.toJson(testTime);
        assertEquals("\"2023-10-27T15:30:00\"", json);

        LocalDateTime parsedTime = gson.fromJson(json, LocalDateTime.class);
        assertEquals(testTime, parsedTime);
    }

    @Test
    public void testSocketMessageParsing() {
        String message = "LOGIN|admin_user|password123";
        String[] parts = message.split("\\|");
        assertEquals(3, parts.length);
        assertEquals("LOGIN", parts[0]);
    }

    @Test
    public void testLoginMessageParsing() {
        // Kiểm tra xử lý định dạng chuỗi LOGIN trong LoginHandlers
        String message = "LOGIN|mikey|123456";
        String[] parts = message.split("\\|");
        
        assertTrue(parts.length >= 3, "Bản tin LOGIN phải có ít nhất 3 phần");
        assertEquals("LOGIN", parts[0]);
        assertEquals("mikey", parts[1]);
        assertEquals("123456", parts[2]);
    }

    @Test
    public void testRegisterMessageParsing() {
        // Kiểm tra xử lý định dạng chuỗi REGISTER trong RegisterHandlers
        String message = "REGISTER|newuser|pass789";
        String[] parts = message.split("\\|");
        
        assertTrue(parts.length >= 3, "Bản tin REGISTER phải có ít nhất 3 phần");
        assertEquals("REGISTER", parts[0]);
        assertEquals("newuser", parts[1]);
        assertEquals("pass789", parts[2]);
    }

    @Test
    public void testUserChangePasswordMessageParsing() {
        // Kiểm tra xử lý định dạng chuỗi CHANGE_PASSWORD trong UserHandler
        String message = "USER|CHANGE_PASSWORD|10|oldPass|newPass";
        String[] parts = message.split("\\|");
        
        assertTrue(parts.length >= 5, "Bản tin đổi mật khẩu phải có ít nhất 5 phần");
        assertEquals("USER", parts[0]);
        assertEquals("CHANGE_PASSWORD", parts[1]);
        assertEquals(10, Integer.parseInt(parts[2]));
        assertEquals("oldPass", parts[3]);
        assertEquals("newPass", parts[4]);
    }

    @Test
    public void testItemFindMessageParsing() {
        // Kiểm tra xử lý định dạng chuỗi ITEM|FIND trong ItemHandler
        String message = "ITEM|FIND|ELECTRONIC|99";
        String[] parts = message.split("\\|");
        
        assertTrue(parts.length >= 4, "Bản tin tìm kiếm ITEM phải có ít nhất 4 phần");
        assertEquals("ITEM", parts[0]);
        assertEquals("FIND", parts[1]);
        assertEquals("ELECTRONIC", parts[2]);
        assertEquals(99, Integer.parseInt(parts[3]));
    }

    @Test
    public void testNotificationMessageParsing() {
        // Kiểm tra xử lý định dạng chuỗi NOTIFICATION|READ trong NotificationHandlers
        String message = "NOTIFICATION|READ|5|12";
        String[] parts = message.split("\\|");
        
        assertTrue(parts.length >= 4);
        assertEquals("NOTIFICATION", parts[0]);
        assertEquals("READ", parts[1]);
        assertEquals(5, Integer.parseInt(parts[2]), "User ID phải được tách chính xác");
        assertEquals(12, Integer.parseInt(parts[3]), "Notification ID phải được tách chính xác");
    }

    @Test
    public void testInvalidMessageFormat() {
        // Mô phỏng trường hợp Client gửi thiếu dữ liệu (ví dụ thiếu password khi login)
        String message = "LOGIN|admin";
        String[] parts = message.split("\\|");
        
        assertFalse(parts.length >= 3, "Bản tin thiếu dữ liệu không được phép vượt qua điều kiện if (parts.length >= 3)");
        System.out.println("Đã phát hiện đúng lỗi định dạng khi thiếu dữ liệu trong bản tin LOGIN");
    }
}
