package com.mikey.auction.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import com.mikey.auction.user.Admin;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Seller;
import com.mikey.auction.user.User;

public class UserDAO extends BaseDAO {
    private static final UserDAO user = new UserDAO();
    private static final String AVATARS_DIR = "avatars";

    static {
        File dir = new File(AVATARS_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private UserDAO(){}
    public static UserDAO getInstance(){
        return user;
    }

    public boolean register(String username, String password){
        String query = "INSERT INTO user(username,password) VALUES (?,?);";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, password);
            st.executeUpdate();
            return true;

        } catch (SQLException ex) {
            System.out.println("add khong thanh cong");
            return false;
        }
    }
    public User findById(int id){
        String query = "SELECT * FROM user WHERE id = ?";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                User u = null;
                String role = rs.getString("role");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String status = rs.getString("status");

                if ("ADMIN".equals(role)) {
                    u = new Admin(username, password, id);
                } else if ("SELLER".equals(role)) {
                    u = new Seller(username, password, id);
                } else {
                    u = new Bidder(username, password, id);
                }

                if (u != null) {
                    u.setStatus(status);
                    u.setAvatar(loadAvatar(id));
                }
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("add khong thanh cong");
        }
    return null;
    }
    
    public User login(String username, String password){
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int id = rs.getInt("id");
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String status = rs.getString("status");

                User u = null;
                if ("ADMIN".equals(role)) {
                    u = new Admin(user, pass, id);
                } else if ("SELLER".equals(role)) {
                    u = new Seller(user, pass, id);
                } else {
                    u = new Bidder(user, pass, id);
                }

                if (u != null) {
                    u.setStatus(status);
                    u.setAvatar(loadAvatar(id));
                }
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("Dang nhap khong thanh cong");
        }
        return null;
    }

    public User findByUsername(String username){
        String query = "SELECT * FROM user WHERE username = ?";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                String password = rs.getString("password");
                String status = rs.getString("status");

                User u = new Bidder(username, password, id);
                u.setStatus(status);
                u.setAvatar(loadAvatar(id));
                return u;
            }
        } catch (SQLException ex) {
            System.out.println("Lỗi tìm user: " + ex.getMessage());
        }
        return null;
    }
    public boolean changePassword(int userId, String newPassword){
        String query = "Update user set password = ? where id = ?";
        try (Connection connection = getConnect()){
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setString(1, newPassword);
            pr.setInt(2, userId);
            return pr.executeUpdate() > 0;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean checkPassword(int userId, String password){
        String query = "SELECT password FROM user WHERE id = ?";
        try (Connection connection = getConnect()){
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setInt(1, userId);
            ResultSet rs = pr.executeQuery();
            if (rs.next()){
                return rs.getString("password").equals(password);
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return false;
    }
    public boolean deleteUser(int userId, String password){
        String query = "DELETE FROM user WHERE id = ? AND password = ?";
        try (Connection connection = getConnect()){
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setInt(1, userId);
            pr.setString(2, password);
            return pr.executeUpdate() > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    // THÊM VÀO CUỐI FILE UserDAO.java
    public java.util.ArrayList<User> getAllUsers() {
        ArrayList<User> list = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Connection connect = getConnect();
             PreparedStatement st = connect.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String uname = rs.getString("username");
                String pass = rs.getString("password");
                String role = rs.getString("role");
                String status = rs.getString("status");

                User u = null;
                if ("ADMIN".equals(role)) u = new Admin(uname, pass, id);
                else if ("SELLER".equals(role)) u = new Seller(uname, pass, id);
                else u = new Bidder(uname, pass, id);

                if (u != null) {
                    u.setStatus(status);
                    u.setAvatar(loadAvatar(id));
                    list.add(u);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Lỗi khi lấy danh sách user: " + ex.getMessage());
        }
        return list;
    }

    public boolean updateUserStatus(int userId, String newStatus) {
        String query = "UPDATE user SET status = ? WHERE id = ?";
        try (Connection connection = getConnect();
             PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setString(1, newStatus);
            pr.setInt(2, userId);
            return pr.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAvatar(int userId, String base64Image) {
        try {
            File avatarFile = new File(AVATARS_DIR + "/avatar_" + userId + ".png");
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            try (FileOutputStream fos = new FileOutputStream(avatarFile)) {
                fos.write(imageBytes);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String loadAvatar(int userId) {
        try {
            File avatarFile = new File(AVATARS_DIR + "/avatar_" + userId + ".png");
            if (avatarFile.exists()) {
                byte[] fileContent = Files.readAllBytes(avatarFile.toPath());
                return Base64.getEncoder().encodeToString(fileContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 🔥 THÊM MỚI: Hàm cập nhật Tên người dùng (Username) vào Database
    public boolean updateUsername(int userId, String newUsername) {
        String query = "UPDATE user SET username = ? WHERE id = ?";
        try (Connection connection = getConnect();
             PreparedStatement pr = connection.prepareStatement(query)) {
            pr.setString(1, newUsername);
            pr.setInt(2, userId);
            return pr.executeUpdate() > 0; // Trả về true nếu cập nhật thành công dòng dữ liệu
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thực hiện cập nhật username: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
