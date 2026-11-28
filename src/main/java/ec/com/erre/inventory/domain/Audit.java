package ec.com.erre.inventory.domain;

import java.time.Instant;

/**
 * Who created a record and when, and who changed it last. Filled in by the storage; times in UTC.
 *
 * @param createdBy username from the token, or {@code system}
 * @param updatedBy null until the first change
 * @param updatedAt null until the first change
 */
public record Audit(String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {}
