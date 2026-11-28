package ec.com.erre.inventory.application;

/**
 * A field of the request is well formed but cannot be used: it points to something that does not
 * exist or is inactive, or it does not apply. The REST adapter answers 400 for that field.
 */
public class InvalidFieldException extends RuntimeException {

    /** Why the field cannot be used. */
    public enum Reason {
        NOT_FOUND,
        INACTIVE,
        NOT_APPLICABLE,
        EXCEEDS_GROSS
    }

    private final String field;
    private final Reason reason;

    public InvalidFieldException(String field, Reason reason) {
        super("Invalid field " + field + ": " + reason);
        this.field = field;
        this.reason = reason;
    }

    public String field() {
        return field;
    }

    public Reason reason() {
        return reason;
    }
}
