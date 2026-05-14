package me.bechberger.shop.service;

import java.util.*;
import java.util.stream.Collectors;

import me.bechberger.shop.model.Money;
import me.bechberger.shop.model.Product;

/**
 * In-memory product catalog with search and filtering.
 */
public class ProductCatalog {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product getById(String id) {
        return findById(id).orElseThrow(() ->
                new NoSuchElementException("Product not found: " + id));
    }

    public List<Product> findByCategory(String category) {
        return products.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Product> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public List<Product> findByPriceRange(Money min, Money max) {
        return products.values().stream()
                .filter(p -> !p.getPrice().isGreaterThan(max) && !min.isGreaterThan(p.getPrice()))
                .collect(Collectors.toList());
    }

    public List<Product> getAll() {
        return List.copyOf(products.values());
    }

    public int size() {
        return products.size();
    }

    public void removeProduct(String id) {
        products.remove(id);
    }

    public List<String> getCategories() {
        return products.values().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
