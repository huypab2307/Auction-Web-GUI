package com.mikey.auction.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.user.Seller;
import com.mikey.auction.user.User;

public class UserManagerTest {

    @Test
    public void testCreateUserBidder() {
        // Kiểm tra tạo Bidder từ User
        User user = new Bidder("bidder_user", "password123", 1);
        User result = UserManager.getInstance().createUser(Role.BIDDER, user);

        assertNotNull(result, "Kết quả không được null");
        assertInstanceOf(Bidder.class, result, "Kết quả phải là instance của Bidder");
        assertEquals("bidder_user", result.getUsername(), "Username phải khớp");
        assertEquals(1, result.getId(), "ID phải khớp");
    }

    @Test
    public void testCreateUserSeller() {
        // Kiểm tra tạo Seller từ User
        User user = new Seller("seller_user", "password456", 2);
        User result = UserManager.getInstance().createUser(Role.SELLER, user);

        assertNotNull(result, "Kết quả không được null");
        assertInstanceOf(Seller.class, result, "Kết quả phải là instance của Seller");
        assertEquals("seller_user", result.getUsername(), "Username phải khớp");
        assertEquals(2, result.getId(), "ID phải khớp");
    }

    @Test
    public void testCreateUserInvalidRole() {
        // Kiểm tra exception khi role không hợp lệ
        User user = new Bidder("test", "pass", 1);

        assertThrows(IllegalArgumentException.class, () -> {
            UserManager.getInstance().createUser(null, user);
        }, "Phải ném IllegalArgumentException cho role không hợp lệ");
    }

    @Test
    public void testCreateBidderPassword() {
        // Kiểm tra mật khẩu được giữ nguyên khi tạo Bidder
        User user = new Bidder("test_user", "secure_password", 5);
        Bidder bidder = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, user);

        assertEquals("secure_password", bidder.getPassword(), "Mật khẩu phải giữ nguyên");
    }

    @Test
    public void testCreateSellerPassword() {
        // Kiểm tra mật khẩu được giữ nguyên khi tạo Seller
        User user = new Seller("seller_test", "seller_pass", 10);
        Seller seller = (Seller) UserManager.getInstance().createUser(Role.SELLER, user);

        assertEquals("seller_pass", seller.getPassword(), "Mật khẩu phải giữ nguyên");
    }

    @Test
    public void testCreateMultipleBidders() {
        // Kiểm tra tạo nhiều Bidder
        User user1 = new Bidder("bidder1", "pass1", 1);
        User user2 = new Bidder("bidder2", "pass2", 2);

        Bidder bidder1 = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, user1);
        Bidder bidder2 = (Bidder) UserManager.getInstance().createUser(Role.BIDDER, user2);

        assertNotEquals(bidder1.getId(), bidder2.getId(), "Các Bidder phải có ID khác nhau");
        assertNotEquals(bidder1.getUsername(), bidder2.getUsername(), "Các Bidder phải có username khác nhau");
    }

    @Test
    public void testCreateMultipleSellers() {
        // Kiểm tra tạo nhiều Seller
        User user1 = new Seller("seller1", "pass1", 3);
        User user2 = new Seller("seller2", "pass2", 4);

        Seller seller1 = (Seller) UserManager.getInstance().createUser(Role.SELLER, user1);
        Seller seller2 = (Seller) UserManager.getInstance().createUser(Role.SELLER, user2);

        assertNotEquals(seller1.getId(), seller2.getId(), "Các Seller phải có ID khác nhau");
        assertNotEquals(seller1.getUsername(), seller2.getUsername(), "Các Seller phải có username khác nhau");
    }
}
