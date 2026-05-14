package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import me.bechberger.shop.util.MoneyFormatter;
import me.bechberger.shop.util.StringSanitizer;

/**
 * Formats and "sends" order notifications (confirmation emails, receipts).
 */
public class NotificationService {

    private final MoneyFormatter moneyFormatter;
    private final StringSanitizer sanitizer;

    public NotificationService() {
        this.moneyFormatter = new MoneyFormatter();
        this.sanitizer = new StringSanitizer();
    }

    /**
     * Build an order confirmation message.
     */
    public String buildConfirmation(Order order) {
        Customer customer = order.getCustomer();
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(sanitizer.sanitize(customer.getName())).append(",\n\n");
        sb.append("Thank you for your order #").append(order.getId()).append("!\n\n");

        sb.append("Items:\n");
        for (OrderItem item : order.getItems()) {
            sb.append("  - ").append(sanitizer.sanitize(item.getProduct().getName()))
              .append(" x").append(item.getQuantity())
              .append(" @ ").append(moneyFormatter.format(item.getUnitPrice()))
              .append(" = ").append(moneyFormatter.format(item.getLineTotal()))
              .append("\n");
        }

        sb.append("\nSubtotal: ").append(moneyFormatter.format(order.getSubtotal()));
        sb.append("\nTax: ").append(moneyFormatter.format(order.getTax()));
        sb.append("\nShipping: ").append(moneyFormatter.format(order.getShippingCost()));
        sb.append("\nTotal: ").append(moneyFormatter.format(order.getTotal()));
        sb.append("\n\nShipping to: ").append(formatAddress(customer.getShippingAddress()));

        return sb.toString();
    }

    /**
     * Build a short receipt (for display or print).
     */
    public String buildReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");
        sb.append("Order: #").append(order.getId()).append("\n");
        sb.append("Customer: ").append(sanitizer.sanitize(order.getCustomer().getName())).append("\n");
        sb.append("Items: ").append(order.getItemCount()).append("\n");
        sb.append("Total: ").append(moneyFormatter.format(order.getTotal())).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("===============\n");
        return sb.toString();
    }

    /**
     * Build a shipping notification.
     */
    public String buildShippingNotification(Order order, String trackingNumber) {
        Customer customer = order.getCustomer();
        return "Hi " + sanitizer.sanitize(customer.getName()) + ",\n\n" +
               "Your order #" + order.getId() + " has been shipped!\n" +
               "Tracking: " + trackingNumber + "\n" +
               "Delivery to: " + formatAddress(customer.getShippingAddress()) + "\n";
    }

    private String formatAddress(Address addr) {
        if (addr == null) return "(no address)";
        return addr.getStreet() + ", " + addr.getCity() + ", " +
               addr.getState() + " " + addr.getZip() + ", " + addr.getCountry();
    }
}
