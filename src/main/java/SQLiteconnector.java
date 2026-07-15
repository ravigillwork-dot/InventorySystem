import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteconnector{
    private String URL;

    public SQLiteconnector(String URL){
        this.URL=URL;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
