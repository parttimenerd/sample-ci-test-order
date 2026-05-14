package me.bechberger.shop.validation;

import me.bechberger.shop.model.Address;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates shipping addresses.
 */
public class AddressValidator {

    private static final Pattern US_ZIP = Pattern.compile("^\\d{5}(-\\d{4})?$");
    private static final Pattern CA_ZIP = Pattern.compile("^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$");

    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
            "US", "CA", "MX", "GB", "DE", "FR", "JP", "AU"
    );

    /**
     * Validate an address. Returns null if valid, or an error message.
     */
    public String validate(Address address) {
        if (address == null) return "Address is required";
        if (isBlank(address.getStreet())) return "Street is required";
        if (isBlank(address.getCity())) return "City is required";
        if (isBlank(address.getCountry())) return "Country is required";

        String country = address.getCountry().toUpperCase();
        if (!SUPPORTED_COUNTRIES.contains(country)) {
            return "Unsupported country: " + country;
        }

        if (isBlank(address.getZip())) return "ZIP/postal code is required";

        // Country-specific zip validation
        String zipError = validateZip(address.getZip(), country);
        if (zipError != null) return zipError;

        // US addresses require state
        if ("US".equals(country) && isBlank(address.getState())) {
            return "State is required for US addresses";
        }

        return null; // valid
    }

    /**
     * Check if the address is in a supported country.
     */
    public boolean isSupportedCountry(String country) {
        return country != null && SUPPORTED_COUNTRIES.contains(country.toUpperCase());
    }

    private String validateZip(String zip, String country) {
        return switch (country) {
            case "US" -> US_ZIP.matcher(zip).matches() ? null : "Invalid US ZIP code: " + zip;
            case "CA" -> CA_ZIP.matcher(zip).matches() ? null : "Invalid Canadian postal code: " + zip;
            default -> null; // no specific validation for other countries
        };
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
