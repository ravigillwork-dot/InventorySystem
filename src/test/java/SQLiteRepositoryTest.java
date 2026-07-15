import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteRepositoryTest {
    private SQLiteRepository repo;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        SQLiteinit init = new SQLiteinit(connection);
        init.initialize();
        repo = new SQLiteRepository(connection);
    }

    // Helper: insert a row directly via raw SQL, bypassing repo.save()
    private void insertRaw(long id, String name, String category, String subCategory,
                           double price, int quantity) throws SQLException {
        String sql = """
                INSERT INTO inventory(productId, name, category, subCategory, price, quantity)
                VALUES (?,?,?,?,?,?);
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, subCategory);
            stmt.setDouble(5, price);
            stmt.setInt(6, quantity);
            stmt.execute();
        }
    }

    // save()
    @Test
    void saveProductAndConfirmCorrectSave() throws SQLException {
        Product p = new Product(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);
        repo.save(p);

        String sql = """
                SELECT * FROM inventory
                WHERE productID = 1;
                """;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(1L, rs.getLong(1));
            assertEquals("Ravi Mouse", rs.getString(2));
            assertEquals("Peripherals", rs.getString(3));
            assertEquals("Mouse", rs.getString(4));
            assertEquals(19.99, rs.getDouble(5));
            assertEquals(9, rs.getInt(6));
        }
    }

    @Test
    void saveDuplicateIdThrowsRepositoryException() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);
        Product duplicate = new Product(1L, "Other Mouse", "Peripherals", "Mouse", 9.99, 5);

        assertThrows(RepositoryException.class, () -> repo.save(duplicate));
    }

    // findAll()
    @Test
    void findAllReturnsAllRows() throws SQLException {
        insertRaw(1L, "Mouse", "Peripherals", "Mouse", 19.99, 9);
        insertRaw(2L, "Keyboard", "Peripherals", "Keyboard", 49.99, 4);

        List<Product> products = repo.findAll();

        assertEquals(2, products.size());
    }

    @Test
    void findAllReturnsEmptyListWhenTableIsEmpty() {
        List<Product> products = repo.findAll();

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    // findById()
    @Test
    void findByIdReturnsCorrectProduct() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        Product p = repo.findById(1L);

        assertNotNull(p);
        assertEquals(1L, p.getProductId());
        assertEquals("Ravi Mouse", p.getName());
        assertEquals("Peripherals", p.getCategory());
        assertEquals("Mouse", p.getSubCategory());
        assertEquals(19.99, p.getPrice());
        assertEquals(9, p.getQuantity());
    }

    @Test
    void findByIdReturnsNullWhenProductDoesNotExist() {
        Product p = repo.findById(999L);

        assertNull(p);
    }

    // deleteById()
    @Test
    void deleteByIdRemovesProduct() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.deleteById(1L);

        String sql = "SELECT * FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertFalse(rs.next());
        }
    }

    @Test
    void deleteByIdOnNonExistentIdDoesNotThrow() {
        assertDoesNotThrow(() -> repo.deleteById(999L));
    }

    // addQuantity()
    @Test
    void addQuantityIncreasesStock() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.addQuantity(1L, 5);

        String sql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(14, rs.getInt(1));
        }
    }

    @Test
    void addQuantityWithNegativeAmountDoesNotChangeStock() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.addQuantity(1L, -5);

        String sql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(9, rs.getInt(1));
        }
    }

    @Test
    void addQuantityOnNonExistentProductDoesNotThrow() {
        assertDoesNotThrow(() -> repo.addQuantity(999L, 5));
    }

    // removeQuantity()
    @Test
    void removeQuantityDecreasesStock() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.removeQuantity(1L, 4);

        String sql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(5, rs.getInt(1));
        }
    }

    @Test
    void removeQuantityBelowZeroDoesNotChangeStock() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.removeQuantity(1L, 20);

        String sql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(9, rs.getInt(1));
        }
    }

    @Test
    void removeQuantityWithNegativeAmountDoesNotChangeStock() throws SQLException {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        repo.removeQuantity(1L, -3);

        String sql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(9, rs.getInt(1));
        }
    }

    @Test
    void removeQuantityOnNonExistentProductDoesNotThrow() {
        assertDoesNotThrow(() -> repo.removeQuantity(999L, 5));
    }
}