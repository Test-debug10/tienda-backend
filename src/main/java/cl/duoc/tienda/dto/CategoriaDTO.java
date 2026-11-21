package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.Categoria;

public record CategoriaDTO(Long id, String nombre, String descripcion) {
    public static CategoriaDTO desde(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion());
    }
}
