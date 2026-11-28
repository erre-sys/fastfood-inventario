package ec.com.erre.inventory.infrastructure.in.rest;

/** Formats from the contract, shared by the request validations. */
final class ApiPatterns {

    /** {@code UnitCodeValue}: lowercase, digits, hyphen or underscore; 1 to 20 characters. */
    static final String UNIT_CODE = "^[a-z0-9][a-z0-9_-]{0,19}$";

    /** {@code BaseFactor}: up to 12 integer digits and 6 decimals. */
    static final String BASE_FACTOR = "^\\d{1,12}(\\.\\d{1,6})?$";

    /** {@code QuantityValue}: non-negative, up to 12 integer digits and 3 decimals. */
    static final String QUANTITY = "^\\d{1,12}(\\.\\d{1,3})?$";

    /** Sort parameter of the unit list: field and optional direction. */
    static final String UNIT_SORT = "^(code|name|dimension)(,(asc|desc))?$";

    static final int NAME_MAX = 60;

    private ApiPatterns() {}
}
