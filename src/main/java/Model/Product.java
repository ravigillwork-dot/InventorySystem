package Model;

public class Product {
    private long productId;
    private String name;
    private String category;
    private String subCategory;
    private double price;
    private int quantity;

    public Product(long productId, String name, String category, String subCategory, double price, int quantity){
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.price = price;
        this.quantity = quantity;
    }

    public Product(){}

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString(){
        return "ID=" + productId + ", Name=" + name + ", Category=" + category
                + ", SubCategory=" + subCategory + ", Price=" + price
                + ", Quantity=" + quantity;
    }
}

