package ec.com.erre.inventory.domain;

/** No unit has the given code. */
public class UnitNotFoundException extends RuntimeException {

    private final String code;

    public UnitNotFoundException(String code) {
        super("Unit not found: " + code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
