package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.DetalleOrden;
import cl.duoc.tienda.model.Orden;

import java.time.LocalDateTime;
import java.util.List;

public record OrdenDTO(
        Long id,
        LocalDateTime fecha,
        String estado,
        Long usuarioId,
        String nombre,
        String apellidos,
        String correo,
        String calle,
        String departamento,
        String region,
        String comuna,
        String indicaciones,
        Integer total,
        List<Detalle> detalles
) {
    public record Detalle(
            Long productoId,
            String nombreProducto,
            String imagen,
            Integer cantidad,
            Integer precioUnitario,
            Integer subtotal
    ) {
        static Detalle desde(DetalleOrden d) {
            return new Detalle(
                    d.getProducto() != null ? d.getProducto().getId() : null,
                    d.getNombreProducto(),
                    d.getProducto() != null ? d.getProducto().getImagen() : null,
                    d.getCantidad(),
                    d.getPrecioUnitario(),
                    d.getSubtotal()
            );
        }
    }

    public static OrdenDTO desde(Orden o) {
        return new OrdenDTO(
                o.getId(),
                o.getFecha(),
                o.getEstado().name(),
                o.getUsuario() != null ? o.getUsuario().getId() : null,
                o.getNombre(),
                o.getApellidos(),
                o.getCorreo(),
                o.getCalle(),
                o.getDepartamento(),
                o.getRegion(),
                o.getComuna(),
                o.getIndicaciones(),
                o.getTotal(),
                o.getDetalles().stream().map(Detalle::desde).toList()
        );
    }
}
