package me.bechberger.shop.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An order placed by a customer.
 */
public class Order {

    public enum Status {
        DRAFT, VALIDATED, PRICED, CONFIRMED, SHIPPED, CANCELLED
    }

    private final String id;
    private final Customer customer;
    private final List<OrderItem> items;
    private Status status;
    private Money subtotal;
    private Money tax;
    private Money shippingCost;
    private Money total;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(String id, Customer customer) {
        this.id = Objects.requireNonNull(id);
        this.customer = Objects.requireNonNull(customer);
        this.items = new ArrayList<>();
        this.status = Status.DRAFT;
        this.subtotal = Money.ZERO;
        this.tax = Money.ZERO;
        this.shippingCost = Money.ZERO;
        this.total = Money.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Status getStatus() { return status; }
    public Money getSubtotal() { return subtotal; }
    public Money getTax() { return tax; }
    public Money getShippingCost() { return shippingCost; }
    public Money getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void addItem(OrderItem item) {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Cannot add items to a " + status + " order");
        }
        items.add(Objects.requireNonNull(item));
        updatedAt = Instant.now();
    }

    public void removeItem(Product product) {
        if (status != Status.DRAFT) {
            throw new IllegalStateException("Cannot remove items from a " + status + " order");
        }
        items.removeIf(item -> item.getProduct().equals(product));
        updatedAt = Instant.now();
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setSubtotal(Money subtotal) { this.subtotal = subtotal; }
    public void setTax(Money tax) { this.tax = tax; }
    public void setShippingCost(Money shippingCost) { this.shippingCost = shippingCost; }
    public void setTotal(Money total) { this.total = total; }

    /**
     * Cancel this order. Only confirmed or shipped orders can be cancelled.
     */
    public void cancel() {
        if (status != Status.CONFIRMED && status != Status.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order in status: " + status);
        }
        this.status = Status.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Total weight of all items in the order.
     */
    public double getTotalWeight() {
        return items.stream().mapToDouble(OrderItem::getTotalWeight).sum();
    }

    /**
     * Number of distinct products in the order.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Total quantity of all items.
     */
    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    @Override
    public String toString() {
        return "Order{" + id + ", " + status + ", items=" + items.size() + ", total=" + total + "}";
    }
}
