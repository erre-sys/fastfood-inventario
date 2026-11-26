package ec.com.erre.fastfood.inventario.infraestructura.salida.bd;

import ec.com.erre.fastfood.inventario.dominio.Unidad;
import ec.com.erre.fastfood.inventario.dominio.puerto.UnidadRepositorio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto del dominio con JPA. */
@Repository
class UnidadRepositorioJpa implements UnidadRepositorio {

    private final UnidadJpaRepositorio jpa;

    UnidadRepositorioJpa(UnidadJpaRepositorio jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Unidad> listar() {
        return jpa.findAll(Sort.by("dimension", "factorABase")).stream()
                .map(UnidadEntidad::aDominio)
                .toList();
    }

    @Override
    public Optional<Unidad> buscarPorCodigo(String codigo) {
        return jpa.findById(codigo).map(UnidadEntidad::aDominio);
    }

    @Override
    public boolean existe(String codigo) {
        return jpa.existsById(codigo);
    }

    @Override
    public Unidad guardar(Unidad unidad) {
        return jpa.save(UnidadEntidad.desde(unidad)).aDominio();
    }
}
