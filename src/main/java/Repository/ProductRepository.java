package Repository;

import Model.Product;

import java.util.List;

public interface ProductRepository {
    void save(Product p);
    List<Product> findAll();
    Product findById(long productId);
    void deleteById(long productId);
    void addQuantity(long productId, int add);
    void removeQuantity(long productId, int remove);
}
