package com.mikey.auction.manager;

import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.user.Seller;
import com.mikey.auction.user.User;


public class UserManager {
    private static final UserManager instance = new UserManager();
    private UserManager() {
    }
    public static UserManager getInstance() {
        return instance;
    }

    public User createUser(Role role, User user) {
        switch (role) {
            case BIDDER:
                return new Bidder(user.getUsername(), user.getPassword(),user.getId());
            case SELLER:
                return new Seller(user.getUsername(), user.getPassword(),user.getId());
            default:
                throw new IllegalArgumentException("Invalid role");
        }

    }

}