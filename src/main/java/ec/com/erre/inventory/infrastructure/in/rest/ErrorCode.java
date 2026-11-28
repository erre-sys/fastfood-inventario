package ec.com.erre.inventory.infrastructure.in.rest;

/**
 * Stable error identifiers for clients (contract: {@code Problem.code}). Titles and details are in
 * {@code messages.properties}, under {@code problem.<CODE>.title} and {@code
 * problem.<CODE>.detail}.
 */
enum ErrorCode {
    VALIDATION_FAILED,
    UNIT_NOT_FOUND,
    UNIT_ALREADY_EXISTS,
    INVALID_CONVERSION,
    FORBIDDEN,
    UNSUPPORTED_MEDIA_TYPE
}
