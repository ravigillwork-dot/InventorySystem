package Repository;

import Model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryRepository implements  ProductRepository{
    private final Map<Long, Product> inventory = new HashMap<>();

    @Override
    public void save(Product p){
        inventory.put(p.getProductId(), p);
        System.out.println("Saved to memory: " + p.getName());
    }

    @Override
    public List<Product> findAll(){
       return new ArrayList<>(inventory.values());
    }

    @Override
    public Product findById(long productId){
        return inventory.get(productId);
    }

    @Override
    public void deleteById(long productId){
        Product removed =inventory.remove(productId);
        if (removed == null){
            System.out.println("Product ID does not exist in the inventory");
            return;
        }
        System.out.println("Product has been deleted from inventory" );
    }

    @Override
    public void addQuantity(long productId, int add) {
        if (add < 0){
            System.out.println("You can not add a negative number.");
            return;
        }
        Product p = findById(productId);
        if (p==null){
            System.out.println("ProductId does not exist");
            return;
        }
        p.setQuantity(p.getQuantity()+ add);
        System.out.println("New inventory for " + p.getName() +" is "+ p.getQuantity());
    }

    @Override
    public void removeQuantity(long productId, int remove) {
        if (remove < 0){
            System.out.println("You can not remove a negative number");
            return;
        }
        Product p = findById(productId);
        if (p==null) {
            System.out.println("ProductId does not exist");
            return;
        }
        if (p.getQuantity() < remove) {
            System.out.println("Inventory of "+ p.getName() +" is less than what you are trying to remove.");
            return;
        }
        p.setQuantity(p.getQuantity()- remove);
        System.out.println("New inventory for " + p.getName() +" is "+ p.getQuantity());
    }


}

