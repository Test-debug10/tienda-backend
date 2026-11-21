package cl.duoc.tienda.dto;

import cl.duoc.tienda.model.Usuario;

import java.time.LocalDate;

/** Datos públicos de un usuario. Nunca incluye la contraseña. */
public record UsuarioDTO(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        String rol,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion
) {
    public static UsuarioDTO desde(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getRun(),
                u.getNombre(),
                u.getApellidos(),
                u.getCorreo(),
                u.getRol().name(),
                u.getFechaNacimiento(),
                u.getRegion(),
                u.getComuna(),
                u.getDireccion()
        );
    }
}
