package Database;
import java.sql.Connection;
import java.sql.SQLException;
import com.mysql.cj.jdbc.MysqlDataSource;

public abstract class BaseDAO {
    private static MysqlDataSource dataSource;
    static{
        dataSource = new MysqlDataSource();
        dataSource.setPortNumber(3306);
        dataSource.setDatabaseName("auction");
        dataSource.setServerName("localhost");
        dataSource.setPassword("Duong107@");
        dataSource.setUser("root");
    }
    public Connection getConnect() throws SQLException{
        return dataSource.getConnection();
    }
}
