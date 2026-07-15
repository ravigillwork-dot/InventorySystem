package Repository;

import Model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRepository implements  ProductRepository{
    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<Long, Product> inventory;

    public FileRepository(String filepath){
        this.path = Path.of(filepath);
        ensureFileExists();
    }

    private void ensureFileExists(){

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                Files.writeString(path, "[]");
                System.out.println("File created: " + path);
            }
            catch (IOException e) {
                System.out.println("Failed to create file: " + e.getMessage());
            }
        }
    }

    private void writeAll(Map<Long, Product> inventory){
        try{
            mapper.writeValue(path.toFile(),inventory);
        }
        catch (IOException e){
            System.out.println("Could not write file " + e.getMessage());
        }
    }

    private Map<Long, Product> readAll(){
        try {
            return mapper.readValue(path.toFile(), new TypeReference<Map<Long, Product>>(){});
        } catch (IOException e) {
            System.out.println("Failed to read file "+ e.getMessage());
            return new HashMap<Long, Product>();
        }
    }

    @Override
    public void save(Product p) {
        inventory = readAll();
        inventory.put(p.getProductId(),p);
        writeAll(inventory);
    }

    @Override
    public List<Product> findAll() {
        inventory = readAll();
        return new ArrayList<>(inventory.values());
    }

    @Override
    public Product findById(long productId) {
        inventory = readAll();
        return inventory.get(productId);
    }

    @Override
    public void deleteById(long productId) {
        inventory = readAll();
       if (!inventory.containsKey(productId)) {
           System.out.println("Product not in inventory.");
           return;
       }
           inventory.remove(productId);
           writeAll(inventory);
    }

    @Override
    public void addQuantity(long productId, int add) {
        inventory = readAll();
        if (add < 0){
            System.out.println("You can not add a negative number.");
            return;
        }
        Product p = findById(productId);
        if (p==null){
            System.out.println("Product does not exist");
            return;
        }
        p.setQuantity(p.getQuantity()+ add);
        System.out.println("New inventory for " + p.getName() +" is "+ p.getQuantity());
        writeAll(inventory);
    }

    @Override
    public void removeQuantity(long productId, int remove) {
        inventory = readAll();
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
        writeAll(inventory);
    }
}
