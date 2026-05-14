package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void usDomesticZone() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        assertEquals(1, addr.getShippingZone());
    }

    @Test
    void canadaZone() {
        Address addr = new Address("456 Queen St", "Toronto", "ON", "M5V 2A8", "CA");
        assertEquals(2, addr.getShippingZone());
    }

    @Test
    void mexicoZone() {
        Address addr = new Address("Av Reforma 100", "Mexico City", "CDMX", "06600", "MX");
        assertEquals(2, addr.getShippingZone());
    }

    @Test
    void internationalZone() {
        Address addr = new Address("221B Baker St", "London", "England", "NW1 6XE", "GB");
        assertEquals(3, addr.getShippingZone());
    }

    @Test
    void equality() {
        Address a = new Address("123 Main", "City", "ST", "12345", "US");
        Address b = new Address("123 Main", "City", "ST", "12345", "US");
        assertEquals(a, b);
    }

    @Test
    void toStringFormat() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        String s = addr.toString();
        assertTrue(s.contains("Springfield"));
        assertTrue(s.contains("62701"));
    }
}
