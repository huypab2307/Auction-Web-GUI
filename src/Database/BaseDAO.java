package Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;


public abstract class BaseDAO {
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            String jdbcUrl = "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/auction"
                           + "?useSSL=true"
                           + "&allowPublicKeyRetrieval=true"
                           + "&serverTimezone=Asia/Ho_Chi_Minh"
                           + "&characterEncoding=UTF-8";
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername("PWSDsNZJuvGcy6w.root");
            config.setPassword("8J7BStoMgALGAgFW");

            config.setMaximumPoolSize(10);        
            config.setMinimumIdle(2);              
            config.setConnectionTimeout(30000);     
            config.setMaxLifetime(1800000);        

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            config.setConnectionInitSql("SET time_zone = '+07:00'");

            dataSource = new HikariDataSource(config);
            System.out.println("[INFO] HikariCP Connection Pool đã được khởi tạo thành công.");

        } catch (Exception e) {
            System.err.println("[ERROR] Không thể khởi tạo HikariCP: " + e.getMessage());
            throw new RuntimeException("Lỗi cấu hình Database.");
        }
    }
    public Connection getConnect() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource chưa được khởi tạo!");
        }
        return dataSource.getConnection();
    }
}