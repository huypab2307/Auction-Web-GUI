package com.mikey.auction.user;

//import com.mikey.auction.auction.NotificationManager;


public abstract class User {
    protected String username;
    protected String password;
    protected int id;
    protected Role role;
    // 👉 THÊM BIẾN TRẠNG THÁI NÀY
    protected String status; 
    protected String avatar; // 👉 THÊM BIẾN LƯU ẢNH DẠNG BASE64

     // THÊM GETTER / SETTER CHO ẢNH
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    // Cập nhật lại Constructor gốc của bạn
    public User(String username, String password, int id, Role role) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.role = role;
        this.status = "ACTIVE"; // Mặc định khi tạo mới là ACTIVE
    
    }

    public String getUsername() { 
        return username; 
    }

    public String getPassword() { 
        return password; 
    }

    public int getId() { 
        return id; 
    }

    public Role getRole() { 
        return role; 
    }

    // 👉 THÊM BỘ GETTER / SETTER CHO TRẠNG THÁI
    public String getStatus() { 
        return status; 
    }

    public void setStatus(String status) { 
        this.status = status; 
    }

    public abstract void showRole();
}