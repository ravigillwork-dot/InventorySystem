
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    Scanner scanner = new Scanner(System.in);
    InputHelper helper = new InputHelper(scanner);

    private void run(ProductRepository repo){
        while (true){
            System.out.print("""
                    What would you like to do? 
                    1. See all products in inventory. 
                    2. Add a new product to the inventory. 
                    3. Add to the quantity of an existing product. 
                    4. Remove from the quantity of an existing product. 
                    5. Delete a product from the inventory
                    6. Exit
                    """);
            int firstaction = scanner.nextInt();
            switch (firstaction){
                case 1 -> {
                    for (Product p: repo.findAll()){System.out.println(p);}
                }
                case 2 -> {
                    Product p = helper.createProduct();
                    repo.save(p);
                    System.out.println("Product has been added to the inventory");
                }
                case 3 -> {
                    long productId = helper.collectId();
                    int num = helper.collectint();
                    repo.addQuantity(productId, num);
                    System.out.println("The product quantity has been updated");
                }
                case 4 -> {
                    long productId = helper.collectId();
                    int num = helper.collectint();
                    repo.removeQuantity(productId, num);
                    System.out.println("The product quantity has been updated");
                }
                case 5 -> {
                    long productId = helper.collectId();
                    repo.deleteById(productId);
                    System.out.println("The product has been removed from inventory");
                }
                case 6 -> {
                    scanner.close();
                    System.exit(0);
                }
            }
        }
    }

    void fileMode(){
        String path = "inventory.json";
        FileRepository repo = new FileRepository(path);
        run(repo);
    }

    void inMemoryMode(){
        InMemoryRepository repo = new InMemoryRepository();
        run(repo);
    }
    void sqliteMode() throws SQLException {
        SQLiteconnector connector = new SQLiteconnector("jdbc:sqlite:inventory.db");
        SQLiteinit init = new SQLiteinit(connector.getConnection());
        init.initialize();
        SQLiteRepository repo = new SQLiteRepository(connector.getConnection());
        run(repo);
    }

    void main() throws SQLException {
        System.out.print("""
                Which mode would you like to run? 
                1. In memory mode. Your data will not be saved after the session is complete.
                2. File mode. Your data will be stored in a JSON file and you may come back to it later. 
                3. SQLite mode. Your data will be stored in SQLite database and you may come back to it later.
                4. Exit
                """);
        int mode = scanner.nextInt();
        switch (mode){
            case 1 -> inMemoryMode();
            case 2 -> fileMode();
            case 3 -> sqliteMode();
            case 4 -> {
                scanner.close();
                System.exit(0);
            }

        }

    }
}
