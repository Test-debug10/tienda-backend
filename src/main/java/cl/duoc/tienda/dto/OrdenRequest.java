package cl.duoc.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Datos que envía el checkout. El frontend solo manda id y cantidad de cada producto:
 * los precios y el total los calcula el backend con los datos de la base de datos.
 */
public record OrdenRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        String correo,

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 300)
        String calle,

        @Size(max = 50)
        String departamento,

        @NotBlank(message = "La región es obligatoria")
        String region,

        @NotBlank(message = "La comuna es obligatoria")
        String comuna,

        @Size(max = 300)
        String indicaciones,

        @NotEmpty(message = "El carrito está vacío")
        List<@Valid Item> items
) {
    public record Item(
            @NotNull(message = "Falta el id del producto")
            Long productoId,

            @NotNull(message = "Falta la cantidad")
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = 99, message = "La cantidad máxima por producto es 99")
            Integer cantidad
    ) {
    }
}
