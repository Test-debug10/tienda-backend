package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.Producto;

/** Datos de un producto que se envían al frontend. */
public record ProductoDTO(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Integer precioOferta,
        Integer precioFinal,
        boolean enOferta,
        Integer stock,
        Integer stockCritico,
        boolean stockBajo,
        String imagen,
        String emoji,
        Long categoriaId,
        String categoria
) {
    public static ProductoDTO desde(Producto p) {
        return new ProductoDTO(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getPrecioOferta(),
                p.precioFinal(),
                p.enOferta(),
                p.getStock(),
                p.getStockCritico(),
                p.getStock() <= p.getStockCritico(),
                p.getImagen(),
                p.getEmoji(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre()
        );
    }
}
