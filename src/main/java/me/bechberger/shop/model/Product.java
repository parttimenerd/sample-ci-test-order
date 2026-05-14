package me.bechberger.shop.model;

import java.util.Objects;

/**
 * A product in the catalog.
 */
public class Product {

    private final String id;
    private String name;
    private Money price;
    private String category;
    private double weightKg;

    public Product(String id, String name, Money price, String category, double weightKg) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.price = Objects.requireNonNull(price);
        this.category = Objects.requireNonNull(category);
        this.weightKg = weightKg;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public Money getPrice() { return price; }
    public void setPrice(Money price) { this.price = Objects.requireNonNull(price); }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = Objects.requireNonNull(category); }
    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product p)) return false;
        return id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Product{" + id + ", " + name + ", " + price + "}";
    }
}
