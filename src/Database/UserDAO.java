package Database;
import User.Admin;
import User.Bidder;
import User.Seller;
import User.User;
import java.sql.*;

public class UserDAO extends BaseDAO {
    private static final UserDAO user = new UserDAO();
    private UserDAO(){}
    public static UserDAO getInstance(){
        return user;
    }

    public void register(String username, String password){
        String query = "INSERT INTO user(username,password) VALUES (?,?);";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, password);
            st.executeUpdate();


        } catch (SQLException ex) {
            System.out.println("add khong thanh cong");
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
}
