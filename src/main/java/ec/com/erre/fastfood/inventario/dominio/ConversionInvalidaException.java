package ec.com.erre.fastfood.inventario.dominio;

/** Se intentó convertir entre unidades de dimensiones distintas (por ejemplo, kg a ml). */
public class ConversionInvalidaException extends RuntimeException {

    public ConversionInvalidaException(Unidad origen, Unidad destino) {
        super(
                "No se puede convertir %s (%s) a %s (%s)"
                        .formatted(
                                origen.codigo(),
                                origen.dimension(),
                                destino.codigo(),
                                destino.dimension()));
    }
}
