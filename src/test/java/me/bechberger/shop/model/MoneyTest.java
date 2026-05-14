package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void addSameCurrency() {
        Money a = Money.usd(10.50);
        Money b = Money.usd(3.25);
        assertEquals(Money.usd(13.75), a.add(b));
    }

    @Test
    void addMismatchedCurrencyThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Money.usd(10).add(Money.eur(5)));
    }

    @Test
    void subtractProducesCorrectResult() {
        Money a = Money.usd(20.00);
        Money b = Money.usd(7.50);
        assertEquals(Money.usd(12.50), a.subtract(b));
    }

    @Test
    void multiplyByQuantity() {
        Money price = Money.usd(9.99);
        assertEquals(Money.usd(29.97), price.multiply(3));
    }

    @Test
    void percentage() {
        Money base = Money.usd(200.00);
        assertEquals(Money.usd(16.00), base.percentage(8.0));
    }

    @Test
    void isNegative() {
        assertTrue(Money.usd(-1.00).isNegative());
        assertFalse(Money.usd(0.00).isNegative());
        assertFalse(Money.usd(1.00).isNegative());
    }

    @Test
    void zeroConstant() {
        assertTrue(Money.ZERO.isZero());
        assertEquals(new BigDecimal("0.00"), Money.ZERO.getAmount());
    }

    @Test
    void roundsToTwoDecimalPlaces() {
        Money m = Money.usd(1.005);
        assertEquals(new BigDecimal("1.01"), m.getAmount());
    }
}
