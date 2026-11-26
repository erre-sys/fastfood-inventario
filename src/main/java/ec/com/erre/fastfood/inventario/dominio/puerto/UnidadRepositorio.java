package ec.com.erre.fastfood.inventario.dominio.puerto;

import ec.com.erre.fastfood.inventario.dominio.Unidad;
import java.util.List;
import java.util.Optional;

/** Lo que el dominio necesita para guardar y leer unidades. Lo implementa la infraestructura. */
public interface UnidadRepositorio {

    List<Unidad> listar();

    Optional<Unidad> buscarPorCodigo(String codigo);

    boolean existe(String codigo);

    Unidad guardar(Unidad unidad);
}
