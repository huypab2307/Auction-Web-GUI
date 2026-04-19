package Database;
import Items.Electronics;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ElectronicsDAO extends ItemDAO {
    private static final ElectronicsDAO electronicsDAO = new ElectronicsDAO();
    private ElectronicsDAO(){}
    public static ElectronicsDAO getInstance(){
        return electronicsDAO;
    }
    public void createItem(Electronics electronic){
        String query = "INSERT INTO electronics(itemId,brand,power,voltage,current,status,color,weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try(Connection connection = getConnect()){
            connection.setAutoCommit(false);
            int generatedId = insertBaseItem(connection, electronic, "ELECTRONICS");
            try{
                try (PreparedStatement pr = connection.prepareStatement(query)){
                    pr.setInt(1,generatedId);
                    pr.setString(2,electronic.getBrand());
                    pr.setInt(3,electronic.getPower());
                    pr.setDouble(4,electronic.getVoltage());
                    pr.setDouble(5,electronic.getCurrent());
                    pr.setString(6,electronic.getStatus());
                    pr.setString(7,electronic.getColor());
                    pr.setDouble(8,electronic.getWeight());

                    pr.executeUpdate();
                }
                connection.commit();
                System.out.println("THêm sản phẩm thành công");
            }catch(SQLException ex){
                    connection.rollback();
                    throw ex;
                }
        }catch (SQLException e){
            System.out.println("thêm sản phẩm không thành công");
        }
    }
}
