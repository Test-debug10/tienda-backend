package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.OrdenDTO;
import cl.duoc.tienda.dto.OrdenRequest;
import cl.duoc.tienda.exception.ReglaNegocioException;
import cl.duoc.tienda.model.*;
import cl.duoc.tienda.repository.OrdenRepository;
import cl.duoc.tienda.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias de la lógica de compra usando mocks de los repositorios. */
@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OrdenService ordenService;

    private Producto teclado;
    private Producto mouse;

    @BeforeEach
    void preparar() {
        Categoria categoria = new Categoria("Periféricos", null);
        categoria.setId(1L);

        teclado = new Producto();
        teclado.setId(1L);
        teclado.setNombre("Teclado");
        teclado.setPrecio(50000);
        teclado.setPrecioOferta(40000);
        teclado.setStock(10);
        teclado.setStockCritico(2);
        teclado.setCategoria(categoria);

        mouse = new Producto();
        mouse.setId(2L);
        mouse.setNombre("Mouse");
        mouse.setPrecio(30000);
        mouse.setStock(1);
        mouse.setStockCritico(2);
        mouse.setCategoria(categoria);

        Usuario cliente = new Usuario();
        cliente.setId(5L);
        cliente.setCorreo("cliente@gmail.com");
        cliente.setRol(Rol.CLIENTE);
        lenient().when(usuarioService.buscarPorCorreo("cliente@gmail.com")).thenReturn(cliente);
        lenient().when(productoRepository.findById(1L)).thenReturn(Optional.of(teclado));
        lenient().when(productoRepository.findById(2L)).thenReturn(Optional.of(mouse));
        lenient().when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private OrdenRequest pedido(OrdenRequest.Item... items) {
        return new OrdenRequest("Pedro", "González", "cliente@gmail.com", "Calle 123", null,
                "Metropolitana de Santiago", "Santiago", null, List.of(items));
    }

    @Test
    void calculaTotalConPrecioOfertaYDescuentaStock() {
        OrdenDTO orden = ordenService.crear(pedido(new OrdenRequest.Item(1L, 2), new OrdenRequest.Item(2L, 1)),
                "cliente@gmail.com");

        // 2 teclados a precio oferta (40.000) + 1 mouse (30.000)
        assertEquals(110000, orden.total());
        assertEquals(8, teclado.getStock());
        assertEquals(0, mouse.getStock());
        assertEquals("PAGADA", orden.estado());
        assertEquals(2, orden.detalles().size());
    }

    @Test
    void juntaProductosRepetidos() {
        OrdenDTO orden = ordenService.crear(pedido(new OrdenRequest.Item(1L, 1), new OrdenRequest.Item(1L, 2)),
                "cliente@gmail.com");

        assertEquals(1, orden.detalles().size());
        assertEquals(3, orden.detalles().get(0).cantidad());
        assertEquals(7, teclado.getStock());
    }

    @Test
    void rechazaCompraSinStockSuficiente() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> ordenService.crear(pedido(new OrdenRequest.Item(2L, 3)), "cliente@gmail.com"));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        verify(ordenRepository, never()).save(any());
    }

    @Test
    void cancelarOrdenDevuelveStock() {
        Orden orden = new Orden();
        orden.setId(9L);
        DetalleOrden detalle = new DetalleOrden();
        detalle.setProducto(teclado);
        detalle.setNombreProducto("Teclado");
        detalle.setCantidad(4);
        detalle.setPrecioUnitario(40000);
        detalle.setSubtotal(160000);
        orden.agregarDetalle(detalle);
        when(ordenRepository.findById(9L)).thenReturn(Optional.of(orden));

        ordenService.cambiarEstado(9L, EstadoOrden.CANCELADA);

        assertEquals(14, teclado.getStock());
        assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
    }
}
