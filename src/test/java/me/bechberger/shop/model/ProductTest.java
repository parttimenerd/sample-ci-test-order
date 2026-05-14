package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void createProduct() {
        Product p = new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
        assertEquals("P1", p.getId());
        assertEquals("Widget", p.getName());
        assertEquals(Money.usd(9.99), p.getPrice());
    }

    @Test
    void equalityById() {
        Product a = new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
        Product b = new Product("P1", "Gadget", Money.usd(19.99), "Other", 1.0);
        assertEquals(a, b); // same ID
    }

    @Test
    void differentIdNotEqual() {
        Product a = new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
        Product b = new Product("P2", "Widget", Money.usd(9.99), "Electronics", 0.5);
        assertNotEquals(a, b);
    }

    @Test
    void setPrice() {
        Product p = new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
        p.setPrice(Money.usd(14.99));
        assertEquals(Money.usd(14.99), p.getPrice());
    }

    @Test
    void toStringContainsName() {
        Product p = new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
        assertTrue(p.toString().contains("Widget"));
    }
}
