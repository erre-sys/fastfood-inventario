package ec.com.erre.inventory.domain;

/** A unit with the given code already exists; codes never change, so it cannot be reused. */
public class UnitAlreadyExistsException extends RuntimeException {

    private final String code;

    public UnitAlreadyExistsException(String code) {
        super("Unit already exists: " + code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
