package ec.com.erre.fastfood.inventario.dominio;

import java.math.BigDecimal;

/**
 * Unidad de medida. El factor dice cuántas unidades base contiene: 1 kg = 1000 g.
 *
 * @param codigo identificador corto y estable (kg, ml, und)
 * @param factorABase cantidad de la unidad base de su dimensión; hasta 6 decimales
 */
public record Unidad(String codigo, String nombre, Dimension dimension, BigDecimal factorABase) {

    static final int DECIMALES_FACTOR = 6;

    public Unidad {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la unidad es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad es obligatorio");
        }
        if (dimension == null) {
            throw new IllegalArgumentException("La dimensión de la unidad es obligatoria");
        }
        if (factorABase == null || factorABase.signum() <= 0) {
            throw new IllegalArgumentException("El factor a base debe ser positivo");
        }
        if (factorABase.stripTrailingZeros().scale() > DECIMALES_FACTOR) {
            throw new IllegalArgumentException(
                    "El factor a base admite hasta " + DECIMALES_FACTOR + " decimales");
        }
    }

    public boolean esBase() {
        return factorABase.compareTo(BigDecimal.ONE) == 0;
    }
}
