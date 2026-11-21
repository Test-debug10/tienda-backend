package cl.duoc.tienda.repository;

import cl.duoc.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByOrderByIdAsc();

    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select p from Producto p where p.precioOferta is not null and p.precioOferta > 0 and p.precioOferta < p.precio order by p.nombre")
    List<Producto> findEnOferta();

    @Query("select p from Producto p where p.stock <= p.stockCritico order by p.stock")
    List<Producto> findConStockCritico();
}
