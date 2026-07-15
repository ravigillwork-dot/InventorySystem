package Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteconnectorTest {
    private String url = "jdbc:sqlite::memory:";
    private SQLiteconnector connector;

    @BeforeEach
    void setUp(){
        connector = new SQLiteconnector(url);
    }

    @Test
    void connectionCreated_isNotNull() throws SQLException {
        try (Connection test = connector.getConnection()){
            assertNotNull(test);
            assertTrue(test.isValid(2));
            assertFalse(test.isClosed());
        }
    }

    @Test
    void connectionFails_withBadConnector(){
        SQLiteconnector bad = new SQLiteconnector("jdbc:sqlite:somewrong/file");

        assertThrows(SQLException.class, () -> bad.getConnection());
    }

}
