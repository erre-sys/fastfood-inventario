package ec.com.erre.inventory.application;

/**
 * A field of a merge patch that may be removed: left as it is, removed ({@code null} in the patch)
 * or set to a value. Used where "missing" and "null" mean different things.
 */
public sealed interface Change<T> {

    record Keep<T>() implements Change<T> {}

    record Remove<T>() implements Change<T> {}

    record To<T>(T value) implements Change<T> {

        public To {
            if (value == null) {
                throw new IllegalArgumentException("Use Remove to remove a value");
            }
        }
    }

    static <T> Change<T> keep() {
        return new Keep<>();
    }

    static <T> Change<T> remove() {
        return new Remove<>();
    }

    static <T> Change<T> to(T value) {
        return new To<>(value);
    }

    /** The new value, given the current one. */
    default T applyTo(T current) {
        return switch (this) {
            case Keep<T> _ -> current;
            case Remove<T> _ -> null;
            case To<T> to -> to.value();
        };
    }
}
