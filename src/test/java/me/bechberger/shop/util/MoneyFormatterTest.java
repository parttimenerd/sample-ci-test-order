package me.bechberger.shop.util;

import me.bechberger.shop.model.Money;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoneyFormatterTest {

    private MoneyFormatter formatter = new MoneyFormatter();

    @Test
    void formatUSD() {
        String result = formatter.format(Money.usd(12.50));
        assertTrue(result.contains("12.50") || result.contains("12,50"));
        assertTrue(result.contains("$"));
    }

    @Test
    void formatZero() {
        String result = formatter.format(Money.usd(0.00));
        assertTrue(result.contains("0.00") || result.contains("0,00"));
    }

    @Test
    void formatAmount() {
        String result = formatter.formatAmount(Money.usd(1234.56));
        assertTrue(result.contains("1,234.56") || result.contains("1234.56"));
    }

    @Test
    void formatCompact() {
        String result = formatter.formatCompact(Money.usd(9.99));
        assertEquals("$9.99", result);
    }

    @Test
    void formatCompactEuro() {
        String result = formatter.formatCompact(Money.eur(15.00));
        assertTrue(result.contains("15.00"));
    }
}
