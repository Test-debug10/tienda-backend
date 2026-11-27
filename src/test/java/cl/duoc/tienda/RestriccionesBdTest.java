package cl.duoc.tienda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Las validaciones también están en la base de datos (restricciones CHECK):
 * aunque se escriba SQL directo, saltándose el backend, la base rechaza los valores inválidos.
 */
@SpringBootTest
class RestriccionesBdTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void laBaseNoAceptaStockNegativo() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("update productos set stock = -1 where id = 1"));
    }

    @Test
    void laBaseNoAceptaPrecioCeroNiCantidadCero() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("update productos set precio = 0 where id = 1"));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into detalle_orden (orden_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal) "
                        + "values (999, 1, 'x', 0, 1000, 0)"));
    }

    @Test
    void laBaseAceptaStockEnCero() {
        Integer stock = jdbc.queryForObject("select stock from productos where id = 2", Integer.class);
        assertEquals(1, jdbc.update("update productos set stock = 0 where id = 2"));
        jdbc.update("update productos set stock = ? where id = 2", stock);
    }
}
