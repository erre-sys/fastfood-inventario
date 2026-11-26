package ec.com.erre.fastfood.inventario.infraestructura.salida.bd;

import ec.com.erre.fastfood.inventario.dominio.Dimension;
import ec.com.erre.fastfood.inventario.dominio.Unidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila de la tabla unidad. Solo existe en la infraestructura; el dominio usa Unidad. */
@Entity
@Table(name = "unidad")
@Getter
@Setter
@NoArgsConstructor
class UnidadEntidad {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Dimension dimension;

    @Column(name = "factor_a_base", nullable = false, precision = 18, scale = 6)
    private BigDecimal factorABase;

    @Column(nullable = false)
    private boolean estandar;

    // Los llena la base de datos (DEFAULT). La auditoría con el usuario real llega con la API.
    @Column(
            name = "creado_por",
            nullable = false,
            length = 100,
            insertable = false,
            updatable = false)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false, insertable = false, updatable = false)
    private Instant creadoEn;

    static UnidadEntidad desde(Unidad unidad) {
        var entidad = new UnidadEntidad();
        entidad.setCodigo(unidad.codigo());
        entidad.setNombre(unidad.nombre());
        entidad.setDimension(unidad.dimension());
        entidad.setFactorABase(unidad.factorABase());
        return entidad;
    }

    Unidad aDominio() {
        return new Unidad(codigo, nombre, dimension, factorABase);
    }
}
