package com.mikey.auction.user;

//import com.mikey.auction.auction.NotificationManager;


public abstract class User {
    protected String username;
    protected String password;
    protected int id;
    protected Role role;

    public User(String username, String password, int id, Role role) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    public int getId(){
        return id;
    }

    public Role getRole(){return role;}
//    public void checkNotifications(){
//        NotificationManager.getInstance().readNotification(this);
//    }
    public abstract void showRole();

}