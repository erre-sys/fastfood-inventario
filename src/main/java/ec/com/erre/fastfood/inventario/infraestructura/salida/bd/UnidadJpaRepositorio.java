package ec.com.erre.fastfood.inventario.infraestructura.salida.bd;

import org.springframework.data.jpa.repository.JpaRepository;

interface UnidadJpaRepositorio extends JpaRepository<UnidadEntidad, String> {}
