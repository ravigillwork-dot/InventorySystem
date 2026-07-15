import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteinitTest {
    private SQLiteinit init;


    @Test
    void tableCreatedCheck() throws SQLException {
       Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
       SQLiteinit init = new SQLiteinit(conn);
       init.initialize();

        Statement tablecheck = conn.createStatement();
        ResultSet response = tablecheck.executeQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='inventory';");

        assertTrue(response.next());

    }
}
