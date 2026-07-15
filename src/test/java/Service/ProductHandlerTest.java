package Service;

import Model.Product;
import Repository.SQLiteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ProductHandlerTest {

    private HttpServer server;
    private Connection connection;
    private HttpClient client;
    private ObjectMapper mapper;
    private int port;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        new SQLiteinit(connection).initialize(); // adjust package/import if SQLiteinit lives elsewhere
        SQLiteRepository repo = new SQLiteRepository(connection);

        server = HttpServer.create(new InetSocketAddress(0), 0); // port 0 = pick a free port
        server.createContext("/products", new ProductHandler(repo));
        server.start();
        port = server.getAddress().getPort();

        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws SQLException {
        server.stop(0);
        connection.close();
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/products";
    }

    // Helper
    private void insertRaw(long id, String name, String category, String subCategory,
                           double price, int quantity) throws SQLException {
        String sql = """
                INSERT INTO inventory(productId, name, category, subCategory, price, quantity)
                VALUES (?,?,?,?,?,?);
                """;
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setString(4, subCategory);
            stmt.setDouble(5, price);
            stmt.setInt(6, quantity);
            stmt.execute();
        }
    }

    // GET
    @Test
    void getByIdReturnsProduct() throws Exception {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Product p = mapper.readValue(response.body(), Product.class);
        assertEquals("Ravi Mouse", p.getName());
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void getByIdReturns400OnInvalidId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=notanumber"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void getAllReturnsEveryProduct() throws Exception {
        insertRaw(1L, "Mouse", "Peripherals", "Mouse", 19.99, 9);
        insertRaw(2L, "Keyboard", "Peripherals", "Keyboard", 49.99, 4);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Product[] products = mapper.readValue(response.body(), Product[].class);
        assertEquals(2, products.length);
    }

    // POST
    @Test
    void postCreatesProduct() throws Exception {
        Product p = new Product(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);
        String json = mapper.writeValueAsString(p);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        String checkSql = "SELECT * FROM inventory WHERE productId = 1;";
        try (var stmt = connection.createStatement(); var rs = stmt.executeQuery(checkSql)) {
            assertTrue(rs.next());
            assertEquals("Ravi Mouse", rs.getString(2));
        }
    }

    @Test
    void postWithInvalidJsonReturns400() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl()))
                .POST(HttpRequest.BodyPublishers.ofString("{ not valid json"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    // DELETE
    @Test
    void deleteRemovesProduct() throws Exception {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        String checkSql = "SELECT * FROM inventory WHERE productId = 1;";
        try (var stmt = connection.createStatement(); var rs = stmt.executeQuery(checkSql)) {
            assertFalse(rs.next());
        }
    }

    @Test
    void deleteReturns404WhenNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    // PUT - add
    @Test
    void putAddIncreasesQuantity() throws Exception {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=1&add=5"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        String checkSql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (var stmt = connection.createStatement(); var rs = stmt.executeQuery(checkSql)) {
            assertTrue(rs.next());
            assertEquals(14, rs.getInt(1));
        }
    }

    // PUT - remove
    @Test
    void putRemoveDecreasesQuantity() throws Exception {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=1&remove=4"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        String checkSql = "SELECT quantity FROM inventory WHERE productId = 1;";
        try (var stmt = connection.createStatement(); var rs = stmt.executeQuery(checkSql)) {
            assertTrue(rs.next());
            assertEquals(5, rs.getInt(1));
        }
    }

    @Test
    void putWithNoAddOrRemoveReturns400() throws Exception {
        insertRaw(1L, "Ravi Mouse", "Peripherals", "Mouse", 19.99, 9);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "?productId=1"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    // Unsupported method
    @Test
    void patchReturns405() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl()))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}