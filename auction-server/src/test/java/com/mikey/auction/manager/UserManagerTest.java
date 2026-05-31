package com.mikey.auction.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        assertDoesNotThrow(() -> {
            User user = new Bidder("bidder_user", "password123", 1);
            User result = UserManager.getInstance().createUser(Role.BIDDER, user);

            assertNotNull(result);
            assertInstanceOf(Bidder.class, result);
            assertEquals("bidder_user", result.getUsername());
        });
    }

    @Test
    public void testCreateUserSeller() {
        assertDoesNotThrow(() -> {
            User user = new Seller("seller_user", "password456", 2);
            User result = UserManager.getInstance().createUser(Role.SELLER, user);

            assertNotNull(result);
            assertInstanceOf(Seller.class, result);
            assertEquals("seller_user", result.getUsername());
        });
    }

    @Test
    public void testCreateUserInvalidRole() {
        User user = new Bidder("test", "pass", 1);
        assertThrows(NullPointerException.class, () -> {
            UserManager.getInstance().createUser(null, user);
        }, "Hệ thống phải báo lỗi khi Role bị Null");
    }
}