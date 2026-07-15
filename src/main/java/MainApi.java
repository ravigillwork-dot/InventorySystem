import Repository.SQLiteRepository;
import Service.ProductHandler;
import Service.SQLiteconnector;
import Service.SQLiteinit;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class MainApi {
    public void main() throws SQLException, IOException {
        int port;
        SQLiteconnector connector = new SQLiteconnector("jdbc:sqlite:inventory.db");
        Connection connection = connector.getConnection();
        SQLiteinit init = new SQLiteinit(connection);
        init.initialize();
        SQLiteRepository repo = new SQLiteRepository(connection);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);
        port = server.getAddress().getPort();
        server.createContext("/products" , new ProductHandler(repo));

        server.start();
        System.out.println("Server has started at port: " + port);
    }
}
