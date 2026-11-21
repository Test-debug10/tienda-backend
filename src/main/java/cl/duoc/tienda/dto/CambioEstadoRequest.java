package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoOrden estado
) {
}
