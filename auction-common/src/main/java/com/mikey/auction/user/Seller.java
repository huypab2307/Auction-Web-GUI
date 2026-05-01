package com.mikey.auction.user;


import com.mikey.auction.items.Item;
import com.mikey.auction.items.ItemType;

public class Seller extends User {

    public Seller(String username, String password, int id) {
        super(username, password, id,Role.SELLER);
    }

    @Override
    public void showRole() {
        System.out.println(username + " is a Seller");
    }

}