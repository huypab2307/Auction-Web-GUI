package Database;
import java.sql.*;

import User.Bidder;
import User.User;

public class UserDAO extends BaseDAO {
    private static final UserDAO user = new UserDAO();
    public static UserDAO getUserDAO(){
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
                return new Bidder(rs.getString("username"),rs.getString("password"),id);
            }
        } catch (SQLException ex) {
            System.out.println("add khong thanh cong");
        }
    return null;
    }

    public Bidder login(String username, String password){
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try(Connection connect = getConnect()){
            PreparedStatement st = connect.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                return new Bidder(rs.getString("username"),rs.getString("password"),rs.getInt("id"));
            }
        } catch (SQLException ex) {
            System.out.println("Dang nhap khong thanh cong");
        }
    return null;    
    }
}
