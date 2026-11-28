package ec.com.erre.inventory.infrastructure.in.rest;

/** A merge patch without any field: there is nothing to change (contract: minProperties 1). */
class EmptyPatchException extends RuntimeException {

    EmptyPatchException() {
        super("The patch must contain name or active");
    }
}
