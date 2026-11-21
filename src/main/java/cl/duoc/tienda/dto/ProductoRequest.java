package cl.duoc.tienda.dto;

import jakarta.validation.constraints.*;

/** Datos para crear o actualizar un producto (RF-007). */
public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre puede tener máximo 120 caracteres")
        String nombre,

        @Size(max = 1000, message = "La descripción puede tener máximo 1000 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 1, message = "El precio debe ser mayor a cero")
        Integer precio,

        @Min(value = 1, message = "El precio de oferta debe ser mayor a cero")
        Integer precioOferta,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio")
        @Min(value = 0, message = "El stock crítico no puede ser negativo")
        Integer stockCritico,

        @Size(max = 255)
        String imagen,

        @Size(max = 16)
        String emoji,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId
) {
}
