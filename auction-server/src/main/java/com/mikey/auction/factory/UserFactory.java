package com.mikey.auction.factory;

import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Role;
import com.mikey.auction.user.Seller;
import com.mikey.auction.user.User;

import static com.mikey.auction.user.Role.*;


public class UserFactory {

    public static User createUser(Role role, User user) {
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