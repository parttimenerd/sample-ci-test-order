package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogTest {

    private ProductCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new ProductCatalog();
        catalog.addProduct(new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5));
        catalog.addProduct(new Product("P2", "Gadget", Money.usd(19.99), "Electronics", 1.0));
        catalog.addProduct(new Product("P3", "Book", Money.usd(14.99), "Books", 0.3));
    }

    @Test
    void findById() {
        assertTrue(catalog.findById("P1").isPresent());
        assertEquals("Widget", catalog.findById("P1").get().getName());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(catalog.findById("NOPE").isEmpty());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        assertThrows(java.util.NoSuchElementException.class, () ->
                catalog.getById("NOPE"));
    }

    @Test
    void findByCategory() {
        var electronics = catalog.findByCategory("Electronics");
        assertEquals(2, electronics.size());
    }

    @Test
    void searchByName() {
        var results = catalog.searchByName("wid");
        assertEquals(1, results.size());
        assertEquals("Widget", results.get(0).getName());
    }

    @Test
    void findByPriceRange() {
        var results = catalog.findByPriceRange(Money.usd(10.00), Money.usd(20.00));
        assertEquals(2, results.size()); // Gadget + Book
    }
}
