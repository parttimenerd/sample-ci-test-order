package me.bechberger.shop.validation;

import me.bechberger.shop.model.*;
import me.bechberger.shop.service.InventoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderValidatorTest {

    private OrderValidator validator;
    private InventoryManager inventory;

    @BeforeEach
    void setUp() {
        inventory = new InventoryManager();
        validator = new OrderValidator(new AddressValidator(), new EmailValidator(), inventory);
    }

    private Order createOrder(String email, String zip) {
        Address addr = new Address("123 Main", "City", "ST", zip, "US");
        Customer customer = new Customer("C1", "Alice", email, addr);
        Order order = new Order("O1", customer);
        Product p = new Product("P1", "Widget", Money.usd(10.00), "General", 1.0);
        order.addItem(new OrderItem(p, 1));
        inventory.setStock("P1", 50);
        return order;
    }

    @Test
    void validOrder() {
        var result = validator.validate(createOrder("alice@example.com", "62701"));
        assertTrue(result.isValid());
    }

    @Test
    void invalidEmail() {
        var result = validator.validate(createOrder("not-email", "62701"));
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("email")));
    }

    @Test
    void invalidAddress() {
        var result = validator.validate(createOrder("a@x.com", "BADZIP"));
        assertFalse(result.isValid());
    }

    @Test
    void emptyOrderInvalid() {
        Address addr = new Address("123 Main", "City", "ST", "62701", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        var result = validator.validate(order);
        assertFalse(result.isValid());
    }

    @Test
    void insufficientStock() {
        Order order = createOrder("a@x.com", "62701");
        inventory.setStock("P1", 0); // no stock
        var result = validator.validate(order);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("stock")));
    }

    @Test
    void excessiveQuantityRejected() {
        Address addr = new Address("123 Main", "City", "ST", "62701", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        Product p = new Product("P1", "Widget", Money.usd(10.00), "General", 1.0);
        order.addItem(new OrderItem(p, 101)); // exceeds MAX_QUANTITY_PER_ITEM
        inventory.setStock("P1", 200);
        var result = validator.validate(order);
        assertFalse(result.isValid());
    }

    @Test
    void multipleErrors() {
        Order order = createOrder("bad-email", "BADZIP");
        inventory.setStock("P1", 0);
        var result = validator.validate(order);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 2);
    }

    @Test
    void newStricterQuantityLimit() {
        Address addr = new Address("123 Main", "City", "ST", "62701", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        Product p = new Product("P1", "Widget", Money.usd(10.00), "General", 1.0);
        order.addItem(new OrderItem(p, 51)); // exceeds new limit of 50
        inventory.setStock("P1", 200);
        var result = validator.validate(order);
        assertFalse(result.isValid());
    }

    @Test
    void quantityWithinNewLimit() {
        Address addr = new Address("123 Main", "City", "ST", "62701", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        Product p = new Product("P1", "Widget", Money.usd(10.00), "General", 1.0);
        order.addItem(new OrderItem(p, 50)); // exactly at new limit
        inventory.setStock("P1", 200);
        var result = validator.validate(order);
        assertTrue(result.isValid());
    }
}
