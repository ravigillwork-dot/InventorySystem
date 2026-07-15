package Repository;

import Model.Product;
import Service.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteRepository implements ProductRepository{
private final Connection connection;

public SQLiteRepository(Connection connection){
    this.connection = connection;
}
    @Override
    public void save(Product p) {
        String sqlInsert = """
                INSERT INTO inventory(productId, name, category, subCategory, price, quantity)
                VALUES (?,?,?,?,?,?);
                """;
        try(PreparedStatement stmt = connection.prepareStatement(sqlInsert)){
            stmt.setLong(1, p.getProductId());
            stmt.setString(2,p.getName());
            stmt.setString(3,p.getCategory());
            stmt.setString(4,p.getSubCategory());
            stmt.setDouble(5,p.getPrice());
            stmt.setInt(6,p.getQuantity());
            stmt.execute();
        } catch (SQLException e) {
            throw new RepositoryException("Database failed " + e.getMessage(),e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sqlFindAll = """
                SELECT * FROM inventory;
                """;
        List <Product> list = new ArrayList<>();

        try(Statement stmt = connection.createStatement()){
            try (ResultSet rs = stmt.executeQuery(sqlFindAll)){
                while (rs.next()){
                    long id = rs.getLong(1);
                    String name = rs.getString(2);
                    String category = rs.getString(3);
                    String subCategory = rs.getString(4);
                    double price = rs.getDouble(5);
                    int quantity = rs.getInt(6);
                    Product p = new Product(id, name ,category, subCategory, price,quantity);
                    list.add(p);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Database failed " + e.getMessage(),e);
        }
    }

    @Override
    public Product findById(long productId) {
        String sqlFindById = """
                SELECT * FROM inventory 
                WHERE productId = ? ;
                """;
        try(PreparedStatement stmt = connection.prepareStatement(sqlFindById)){
            stmt.setLong(1, productId);
            try(ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    long id = rs.getLong(1);
                    String name = rs.getString(2);
                    String category = rs.getString(3);
                    String subCategory = rs.getString(4);
                    double price = rs.getDouble(5);
                    int quantity = rs.getInt(6);
                    return new Product(id, name ,category, subCategory, price,quantity);
                } else {
                    System.out.println("Product does not exist in inventory.");
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Database failed " + e.getMessage(),e);
        }
    }

    @Override
    public void deleteById(long productId) {
    String sqlDeleteById = """
            DELETE FROM inventory
            WHERE productId = ? ;
            """;
        try(PreparedStatement stmt = connection.prepareStatement(sqlDeleteById)){
            stmt.setLong(1,productId);
            stmt.execute();
        }catch (SQLException e){
            throw new RepositoryException("Database failed " + e.getMessage(),e);
        }
    }

    @Override
    public void addQuantity(long productId, int add) {
    String sqlAddQuantity = """
            UPDATE inventory
            SET quantity = ?
            WHERE productId = ?;
            """;
        if (add < 0){
            System.out.println("You can not add a negative number.");
            return;
        }
        Product p = findById(productId);
        if (p==null){
            System.out.println("Product does not exist");
            return;
        }
        int quantity = p.getQuantity()+add;

        try(PreparedStatement stmt = connection.prepareStatement(sqlAddQuantity)){
            stmt.setInt(1,quantity);
            stmt.setLong(2, productId);
            stmt.execute();
        }catch (SQLException e){
            throw new RepositoryException("Database failed " + e.getMessage(),e);
        }
    }

    @Override
    public void removeQuantity(long productId, int remove) {
        String sqlRemoveQuantity = """
            UPDATE inventory
            SET quantity = ?
            WHERE productId = ?;
            """;
        if (remove < 0){
            System.out.println("You can not remove a negative number.");
            return;
        }
        Product p = findById(productId);
        if (p==null){
            System.out.println("Product does not exist");
            return;
        }
        int quantity = p.getQuantity()-remove;

        if (quantity>=0){
            try(PreparedStatement stmt = connection.prepareStatement(sqlRemoveQuantity)){
                stmt.setInt(1,quantity);
                stmt.setLong(2, productId);
                stmt.execute();
            }catch (SQLException e){
                throw new RepositoryException("Database failed " + e.getMessage(),e);
            }
        }
        else {
            System.out.println("You can not remove more than is in inventory");
        }
    }

}
