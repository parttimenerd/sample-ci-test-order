package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService service = new NotificationService();

    private Order createSampleOrder() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        Customer customer = new Customer("C1", "Alice Smith", "alice@example.com", addr);
        Order order = new Order("ORD-001", customer);
        Product p = new Product("P1", "Widget", Money.usd(25.00), "Electronics", 0.5);
        order.addItem(new OrderItem(p, 2));
        order.setSubtotal(Money.usd(50.00));
        order.setTax(Money.usd(4.00));
        order.setShippingCost(Money.usd(5.49));
        order.setTotal(Money.usd(59.49));
        order.setStatus(Order.Status.CONFIRMED);
        return order;
    }

    @Test
    void confirmationContainsCustomerName() {
        String msg = service.buildConfirmation(createSampleOrder());
        assertTrue(msg.contains("Alice Smith"));
    }

    @Test
    void confirmationContainsOrderId() {
        String msg = service.buildConfirmation(createSampleOrder());
        assertTrue(msg.contains("ORD-001"));
    }

    @Test
    void confirmationContainsItemDetails() {
        String msg = service.buildConfirmation(createSampleOrder());
        assertTrue(msg.contains("Widget"));
        assertTrue(msg.contains("x2"));
    }

    @Test
    void receiptFormat() {
        String receipt = service.buildReceipt(createSampleOrder());
        assertTrue(receipt.contains("RECEIPT"));
        assertTrue(receipt.contains("ORD-001"));
        assertTrue(receipt.contains("CONFIRMED"));
    }

    @Test
    void shippingNotification() {
        String msg = service.buildShippingNotification(createSampleOrder(), "TRK-12345");
        assertTrue(msg.contains("shipped"));
        assertTrue(msg.contains("TRK-12345"));
    }

    @Test
    void sanitizesHtmlInNames() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "<b>Evil</b>", "e@x.com", addr);
        Order order = new Order("O1", customer);
        Product p = new Product("P1", "<script>alert('xss')</script>Widget", Money.usd(10.00), "G", 1.0);
        order.addItem(new OrderItem(p, 1));
        order.setSubtotal(Money.usd(10.00));
        order.setTax(Money.usd(0.80));
        order.setTotal(Money.usd(10.80));
        String msg = service.buildConfirmation(order);
        assertFalse(msg.contains("<script>"));
        assertFalse(msg.contains("<b>"));
    }
}
