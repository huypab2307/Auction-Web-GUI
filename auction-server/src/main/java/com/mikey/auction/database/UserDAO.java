package com.mikey.auction.database;

import com.mikey.auction.user.Admin;
import com.mikey.auction.user.Bidder;
import com.mikey.auction.user.Seller;
import com.mikey.auction.user.User;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO extends BaseDAO {
    private static final UserDAO user = new UserDAO();
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
                return UserDAO.getInstance().login(rs.getString("username"),rs.getString("password"));
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
                System.out.println(role + id + user + pass);

                if ("ADMIN".equals(role)) {
                    return new Admin(user, pass, id);
                } else if ("SELLER".equals(role)) {
                    return new Seller(user, pass, id);
                } else {
                    return new Bidder(user, pass, id);
                }
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
                return new Bidder(rs.getString("username"), rs.getString("password"), rs.getInt("id"));
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
                
                User u = null;
                if ("ADMIN".equals(role)) {
                    u = new Admin(uname, pass, id);
                } else if ("SELLER".equals(role)) {
                    u = new Seller(uname, pass, id);
                } else {
                    u = new Bidder(uname, pass, id);
                }
                
                if (u != null) {
                    list.add(u);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Lỗi khi lấy danh sách user: " + ex.getMessage());
        }
        return list;
    }
}
