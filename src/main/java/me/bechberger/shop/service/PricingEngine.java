package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import me.bechberger.shop.util.MoneyFormatter;

/**
 * Calculates order totals including discounts and tax.
 */
public class PricingEngine {

    private static final double TAX_RATE = 0.08; // 8% sales tax
    private static final double BULK_DISCOUNT_THRESHOLD = 10;
    private static final double BULK_DISCOUNT_PERCENT = 5.0;
    private static final double LOYALTY_DISCOUNT_PERCENT = 10.0;

    private final MoneyFormatter formatter;

    public PricingEngine() {
        this.formatter = new MoneyFormatter();
    }

    /**
     * Calculate the subtotal for an order (sum of all line items).
     */
    public Money calculateSubtotal(Order order) {
        Money subtotal = Money.ZERO;
        for (OrderItem item : order.getItems()) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return subtotal;
    }

    /**
     * Apply bulk discount: 5% off if total quantity >= 10.
     */
    public Money calculateBulkDiscount(Order order, Money subtotal) {
        if (order.getTotalQuantity() >= BULK_DISCOUNT_THRESHOLD) {
            return subtotal.percentage(BULK_DISCOUNT_PERCENT);
        }
        return Money.ZERO;
    }

    /**
     * Calculate tax on the given amount.
     */
    public Money calculateTax(Money taxableAmount) {
        return taxableAmount.multiply(TAX_RATE);
    }

    /**
     * Full pricing: subtotal - discounts + tax.
     * Returns the final total and updates the order object.
     */
    public Money priceOrder(Order order) {
        Money subtotal = calculateSubtotal(order);
        Money discount = calculateBulkDiscount(order, subtotal);
        Money afterDiscount = subtotal.subtract(discount);
        Money tax = calculateTax(afterDiscount);
        Money total = afterDiscount.add(tax);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotal(total);
        order.setStatus(Order.Status.PRICED);

        return total;
    }

    /**
     * Format a price for display.
     */
    public String formatPrice(Money money) {
        return formatter.format(money);
    }

    /**
     * Calculate loyalty discount: 10% off for loyal customers (5+ orders).
     */
    public Money calculateLoyaltyDiscount(Customer customer, Money subtotal) {
        if (customer.isLoyal()) {
            return subtotal.percentage(LOYALTY_DISCOUNT_PERCENT);
        }
        return Money.ZERO;
    }
}
