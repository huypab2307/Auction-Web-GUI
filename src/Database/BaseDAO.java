package Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.mysql.cj.jdbc.MysqlDataSource;

public abstract class BaseDAO {
    private static MysqlDataSource dataSource;

    static {
        dataSource = new MysqlDataSource();
        try {
            dataSource.setServerName("gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com");
            dataSource.setPortNumber(4000);
            dataSource.setDatabaseName("auction");
            dataSource.setUser("PWSDsNZJuvGcy6w.root");
            dataSource.setPassword("8J7BStoMgALGAgFW");

            // Quan trọng: Ép múi giờ Asia/Ho_Chi_Minh
            dataSource.setServerTimezone("Asia/Ho_Chi_Minh");
            
            dataSource.setUseSSL(true);
            dataSource.setAllowPublicKeyRetrieval(true);
            dataSource.setCharacterEncoding("UTF-8");

        } catch (SQLException e) {
            System.err.println("Lỗi cấu hình DataSource: " + e.getMessage());
        }
    }

    public Connection getConnect() throws SQLException {
        Connection conn = dataSource.getConnection();
        

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET time_zone = '+07:00'");
        } catch (SQLException e) {
            System.err.println("Không thể thiết lập time_zone: " + e.getMessage());
        }
        
        return conn;
    }
}