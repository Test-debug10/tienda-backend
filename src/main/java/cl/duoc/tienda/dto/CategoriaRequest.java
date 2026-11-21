package cl.duoc.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 60, message = "El nombre puede tener máximo 60 caracteres")
        String nombre,

        @Size(max = 255)
        String descripcion
) {
}
