package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.Rol;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Datos que usa el administrador para crear o editar usuarios (RF-008).
 * Al editar, la contraseña puede venir vacía para conservar la actual.
 */
public record UsuarioRequest(
        @NotBlank(message = "El RUN es obligatorio")
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 100)
        String correo,

        @Size(max = 64, message = "La contraseña puede tener máximo 64 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        String region,
        String comuna,

        @Size(max = 300)
        String direccion
) {
}
