
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteinit {
    private Connection connection;
    public SQLiteinit(Connection connection){
        this.connection = connection;
    }

    public void initialize(){
        String createTable = """
                CREATE TABLE IF NOT EXISTS inventory(
                productId INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                subCategory TEXT,
                price REAL NOT NULL,
                quantity INTEGER
                );
                """;

        try(
                Statement stmt = connection.createStatement()){
            stmt.execute(createTable);
        } catch (SQLException e) {
            throw new RepositoryException("Database could not be initialized"+e.getMessage(),e);
        }
    }

}
