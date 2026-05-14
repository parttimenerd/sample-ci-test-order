package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import me.bechberger.shop.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private OrderProcessor processor;
    private InventoryManager inventory;
    private PricingEngine pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingEngine();
        inventory = new InventoryManager();
        ShippingCalculator shipping = new ShippingCalculator();
        AddressValidator addrValidator = new AddressValidator();
        EmailValidator emailValidator = new EmailValidator();
        OrderValidator orderValidator = new OrderValidator(addrValidator, emailValidator, inventory);
        NotificationService notifications = new NotificationService();
        processor = new OrderProcessor(pricing, inventory, shipping, orderValidator, notifications);
    }

    private Order createValidOrder() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        Customer customer = new Customer("C1", "Alice", "alice@example.com", addr);
        Order order = new Order("O1", customer);
        Product product = new Product("P1", "Widget", Money.usd(25.00), "Electronics", 0.5);
        order.addItem(new OrderItem(product, 2));
        inventory.setStock("P1", 100);
        return order;
    }

    @Test
    void successfulOrderProcessing() {
        Order order = createValidOrder();
        var result = processor.processOrder(order);
        assertTrue(result.isSuccess());
        assertEquals(Order.Status.CONFIRMED, result.getOrder().getStatus());
    }

    @Test
    void orderTotalIncludesTaxAndShipping() {
        Order order = createValidOrder();
        var result = processor.processOrder(order);
        assertTrue(result.isSuccess());
        // subtotal=50, tax=4, shipping calculated by weight+zone
        assertFalse(result.getOrder().getTotal().isZero());
    }

    @Test
    void confirmationMessageGenerated() {
        Order order = createValidOrder();
        var result = processor.processOrder(order);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Alice"));
        assertTrue(result.getMessage().contains("O1"));
    }

    @Test
    void invalidEmailRejected() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "not-an-email", addr);
        Order order = new Order("O1", customer);
        order.addItem(new OrderItem(new Product("P1", "X", Money.usd(10), "G", 1), 1));
        inventory.setStock("P1", 10);
        var result = processor.processOrder(order);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("email"));
    }

    @Test
    void emptyOrderRejected() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        var result = processor.processOrder(order);
        assertFalse(result.isSuccess());
    }

    @Test
    void insufficientStockRejected() {
        Order order = createValidOrder();
        inventory.setStock("P1", 1); // only 1 in stock, order wants 2
        var result = processor.processOrder(order);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("stock") || result.getMessage().contains("Insufficient"));
    }

    @Test
    void inventoryReservedOnSuccess() {
        Order order = createValidOrder();
        processor.processOrder(order);
        // 100 initial - 2 reserved then confirmed
        assertTrue(inventory.getAvailable("P1") < 100);
    }

    @Test
    void customerOrderCountIncrements() {
        Order order = createValidOrder();
        Customer customer = order.getCustomer();
        assertEquals(0, customer.getTotalOrders());
        processor.processOrder(order);
        assertEquals(1, customer.getTotalOrders());
    }
}
