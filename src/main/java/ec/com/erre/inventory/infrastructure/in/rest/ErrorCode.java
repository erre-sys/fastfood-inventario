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
    ITEM_NOT_FOUND,
    ITEM_ALREADY_EXISTS,
    CATEGORY_NOT_FOUND,
    CATEGORY_ALREADY_EXISTS,
    FORBIDDEN,
    UNSUPPORTED_MEDIA_TYPE
}
