package Database;

import java.sql.Connection;
import java.sql.SQLException;
import com.mysql.cj.jdbc.MysqlDataSource;

public abstract class BaseDAO {
    private static MysqlDataSource dataSource;

    static {
        try {
            dataSource = new MysqlDataSource();
            dataSource.setServerName("gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com");

            dataSource.setPortNumber(4000);
            dataSource.setDatabaseName("auction");
            dataSource.setUser("PWSDsNZJuvGcy6w.root");
            
            dataSource.setPassword("8J7BStoMgALGAgFW");

            dataSource.setUseSSL(true); 
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