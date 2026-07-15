package Service;

import Model.Product;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHelper {
private final Scanner scanner;

public InputHelper(Scanner scanner){
    this.scanner = scanner;
}
    public long collectId(){
        System.out.print("Please enter a valid product ID: ");
        long id = 0;
        try {
            id = scanner.nextLong();
        } catch (InputMismatchException e) {
            System.out.println("Please enter a number");
            scanner.nextLine();
            return collectId();
        }
        scanner.nextLine(); // To avoid the bug with scanner leaving a /n after long and int.
        return id;
    }
    public String collectName(){
        System.out.print("Please enter the Product name: ");
        return scanner.nextLine();
    }
    public String collectCategory(){
        System.out.print("Please enter the Product Category: ");
        return scanner.nextLine();
    }

    public String collectSubCategory(){
        System.out.print("Please enter the SubCategory: ");
        return scanner.nextLine();
    }

    public double collectPrice() {
        System.out.print("Please enter the price of the item: ");
        double price = 0;
        try {
            price = scanner.nextDouble();
        } catch (InputMismatchException e) {
            System.out.println("Please enter a number");
            scanner.nextLine();
            return collectPrice();
        }
        if (price < 0) {
            System.out.println("The price can not be negative.");
            return collectPrice();
        } else {
            scanner.nextLine(); // To avoid the bug with scanner leaving a /n after long and int.
            return price;
        }
    }

    public int collectQuantity(){
        System.out.print("Please enter the quantity of product to add to inventory: ");
        int quantity = 0;
        try {
            quantity = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Please enter a number");
            scanner.nextLine();
            return collectQuantity();
        }
        if (quantity < 0){
            System.out.println("The amount can not be negative.");
            return collectQuantity();
        }
        else {
            scanner.nextLine(); // To avoid the bug with scanner leaving a /n after long and int.
            return quantity;
        }
    }

    public Product createProduct(){
        long id = collectId();
        String name = collectName();
        String category = collectCategory();
        String subCategory = collectSubCategory();
        double price = collectPrice();
        int quantity = collectQuantity();
        return new Product(id,name,category,subCategory,price,quantity);
    }

    public int collectint(){
        System.out.print("Please enter a number: ");
        int num = 0;
        try {
            num = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Please enter a number");
            scanner.nextLine();
            return collectint();
        }
        if (num < 0){
            System.out.println("The amount can not be negative.");
            return collectint();
        }
        else {
            scanner.nextLine(); // To avoid the bug with scanner leaving a /n after long and int.
            return num;
        }
    }
}
