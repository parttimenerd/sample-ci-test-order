package me.bechberger.shop.util;

import me.bechberger.shop.model.Money;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * Locale-aware money formatting with currency symbols.
 */
public class MoneyFormatter {

    private static final Map<String, Locale> CURRENCY_LOCALES = Map.of(
            "USD", Locale.US,
            "EUR", Locale.GERMANY,
            "GBP", Locale.UK,
            "JPY", Locale.JAPAN,
            "CAD", Locale.CANADA
    );

    /**
     * Format money with locale-appropriate currency symbol.
     * e.g. USD 12.50 → "$12.50", EUR 12.50 → "12,50 €"
     */
    public String format(Money money) {
        Locale locale = CURRENCY_LOCALES.getOrDefault(money.getCurrency(), Locale.US);
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        try {
            fmt.setCurrency(Currency.getInstance(money.getCurrency()));
        } catch (IllegalArgumentException e) {
            // Unknown currency code, fall back to plain format
            return money.getCurrency() + " " + money.getAmount().toPlainString();
        }
        return fmt.format(money.getAmount());
    }

    /**
     * Format without currency symbol (just the number).
     */
    public String formatAmount(Money money) {
        Locale locale = CURRENCY_LOCALES.getOrDefault(money.getCurrency(), Locale.US);
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return fmt.format(money.getAmount());
    }

    /**
     * Compact format for display in tables: "$12.50" or "€12.50".
     */
    public String formatCompact(Money money) {
        String symbol = getCurrencySymbol(money.getCurrency());
        return symbol + money.getAmount().toPlainString();
    }

    private String getCurrencySymbol(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode).getSymbol(Locale.US);
        } catch (IllegalArgumentException e) {
            return currencyCode + " ";
        }
    }
}
