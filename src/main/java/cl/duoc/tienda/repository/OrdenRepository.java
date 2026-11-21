package cl.duoc.tienda.repository;

import cl.duoc.tienda.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findAllByOrderByFechaDesc();
    List<Orden> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}
