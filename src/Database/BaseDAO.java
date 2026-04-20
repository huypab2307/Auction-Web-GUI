package Database;

import java.sql.Connection;
import java.sql.SQLException;
import com.mysql.cj.jdbc.MysqlDataSource;

public abstract class BaseDAO {
    private static MysqlDataSource dataSource;

    static {
        try {
            dataSource = new MysqlDataSource();
            dataSource.setServerName("bnh4szqalzeyaqls8eup-mysql.services.clever-cloud.com");

            dataSource.setPortNumber(3306);
            dataSource.setDatabaseName("bnh4szqalzeyaqls8eup");
            dataSource.setUser("ufyndotvxda0ocgn");
            
            dataSource.setPassword("CCyKF6mywYnkWPG8KEDo");

            dataSource.setUseSSL(false); 
            dataSource.setAllowPublicKeyRetrieval(true); 
            dataSource.setCharacterEncoding("UTF-8"); 

        } catch (SQLException e) {
            System.err.println("Lỗi cấu hình DataSource: " + e.getMessage());
        }
    }

    public Connection getConnect() throws SQLException {
        return dataSource.getConnection();
    }
}