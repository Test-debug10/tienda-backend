package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.OrdenDTO;
import cl.duoc.tienda.dto.OrdenRequest;
import cl.duoc.tienda.exception.RecursoNoEncontradoException;
import cl.duoc.tienda.exception.ReglaNegocioException;
import cl.duoc.tienda.model.*;
import cl.duoc.tienda.repository.OrdenRepository;
import cl.duoc.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de compra. Reglas principales:
 *  1. Los precios y el total se calculan en el servidor con los datos de la BD
 *     (si el frontend manda un precio alterado no tiene efecto).
 *  2. Si un producto no tiene stock suficiente la compra completa se rechaza (409).
 *  3. Al confirmar la compra se descuenta el stock. Todo ocurre en una transacción:
 *     si algo falla no queda nada a medias.
 *  4. Al cancelar una orden se devuelve el stock.
 */
@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;

    public OrdenService(OrdenRepository ordenRepository, ProductoRepository productoRepository,
                        UsuarioService usuarioService) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public OrdenDTO crear(OrdenRequest datos, String correoUsuario) {
        Usuario usuario = usuarioService.buscarPorCorreo(correoUsuario);

        // Junta cantidades si el mismo producto viene repetido
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (OrdenRequest.Item item : datos.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setNombre(datos.nombre().trim());
        orden.setApellidos(datos.apellidos().trim());
        orden.setCorreo(datos.correo().trim().toLowerCase());
        orden.setCalle(datos.calle().trim());
        orden.setDepartamento(datos.departamento());
        orden.setRegion(datos.region());
        orden.setComuna(datos.comuna());
        orden.setIndicaciones(datos.indicaciones());

        int total = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + entrada.getKey() + " no encontrado"));
            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ")");
            }
            producto.setStock(producto.getStock() - cantidad);

            DetalleOrden detalle = new DetalleOrden();
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.precioFinal());
            detalle.setSubtotal(producto.precioFinal() * cantidad);
            orden.agregarDetalle(detalle);
            total += detalle.getSubtotal();
        }
        orden.setTotal(total);
        orden.setEstado(EstadoOrden.PAGADA);
        return OrdenDTO.desde(ordenRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenDTO> listar() {
        return ordenRepository.findAllByOrderByFechaDesc().stream().map(OrdenDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenDTO> listarDeUsuario(String correo) {
        Usuario usuario = usuarioService.buscarPorCorreo(correo);
        return ordenRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream().map(OrdenDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public OrdenDTO obtener(Long id) {
        return OrdenDTO.desde(buscar(id));
    }

    @Transactional
    public OrdenDTO cambiarEstado(Long id, EstadoOrden nuevoEstado) {
        Orden orden = buscar(id);
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new ReglaNegocioException("La orden ya está cancelada");
        }
        if (nuevoEstado == EstadoOrden.CANCELADA) {
            devolverStock(orden);
        }
        orden.setEstado(nuevoEstado);
        return OrdenDTO.desde(orden);
    }

    @Transactional
    public void eliminar(Long id) {
        Orden orden = buscar(id);
        if (orden.getEstado() != EstadoOrden.CANCELADA) {
            devolverStock(orden);
        }
        ordenRepository.delete(orden);
    }

    private void devolverStock(Orden orden) {
        for (DetalleOrden detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() + detalle.getCantidad());
            }
        }
    }

    private Orden buscar(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden " + id + " no encontrada"));
    }
}
