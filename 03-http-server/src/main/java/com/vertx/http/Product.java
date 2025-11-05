package com.vertx.http;

import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Product model for REST API demonstrations
 */
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;

    public Product() {
        this.id = UUID.randomUUID().toString();
    }

    public Product(String name, String description, double price, int quantity) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("name", name)
                .put("description", description)
                .put("price", price)
                .put("quantity", quantity);
    }

    public static Product fromJson(JsonObject json) {
        Product product = new Product();
        product.setId(json.getString("id"));
        product.setName(json.getString("name"));
        product.setDescription(json.getString("description"));
        product.setPrice(json.getDouble("price", 0.0));
        product.setQuantity(json.getInteger("quantity", 0));
        return product;
    }
}
