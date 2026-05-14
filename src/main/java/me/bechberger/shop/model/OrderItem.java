package me.bechberger.shop.model;

import java.util.Objects;

/**
 * A line item in an order — links a product to a quantity.
 */
public class OrderItem {

    private final Product product;
    private int quantity;
    private Money unitPrice; // snapshot of price at order time

    public OrderItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = quantity;
    }
    public Money getUnitPrice() { return unitPrice; }

    /**
     * Total for this line item: unitPrice * quantity.
     */
    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }

    /**
     * Total weight for this line item.
     */
    public double getTotalWeight() {
        return product.getWeightKg() * quantity;
    }

    @Override
    public String toString() {
        return quantity + "x " + product.getName() + " @ " + unitPrice;
    }
}
