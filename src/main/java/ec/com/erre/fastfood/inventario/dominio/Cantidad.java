package ec.com.erre.fastfood.inventario.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Un valor con su unidad: 2.5 kg. Sabe convertirse a otra unidad de la misma dimensión. */
public record Cantidad(BigDecimal valor, Unidad unidad) {

    static final int DECIMALES = 3;

    public Cantidad {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa ni vacía");
        }
        if (unidad == null) {
            throw new IllegalArgumentException("La cantidad necesita una unidad");
        }
    }

    /** El valor expresado en la unidad base de su dimensión (g, ml o und). */
    public BigDecimal enBase() {
        return valor.multiply(unidad.factorABase());
    }

    public Cantidad convertirA(Unidad destino) {
        if (unidad.dimension() != destino.dimension()) {
            throw new ConversionInvalidaException(unidad, destino);
        }
        var convertido = enBase().divide(destino.factorABase(), DECIMALES, RoundingMode.HALF_UP);
        return new Cantidad(convertido, destino);
    }
}
