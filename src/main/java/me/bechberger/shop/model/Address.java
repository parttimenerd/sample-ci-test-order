package me.bechberger.shop.model;

import java.util.Objects;

/**
 * A physical address for shipping.
 */
public class Address {

    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;

    public Address(String street, String city, String state, String zip, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    /**
     * Returns the shipping zone based on country.
     * Zone 1: US domestic, Zone 2: North America, Zone 3: International
     */
    public int getShippingZone() {
        if ("US".equalsIgnoreCase(country)) return 1;
        if ("CA".equalsIgnoreCase(country) || "MX".equalsIgnoreCase(country)) return 2;
        return 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address a)) return false;
        return Objects.equals(street, a.street) && Objects.equals(city, a.city) &&
               Objects.equals(state, a.state) && Objects.equals(zip, a.zip) &&
               Objects.equals(country, a.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zip, country);
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zip + ", " + country;
    }
}
