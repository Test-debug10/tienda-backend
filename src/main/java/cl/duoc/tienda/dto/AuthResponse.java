package cl.duoc.tienda.dto;

/**
 * Respuesta de login, registro y renovación.
 * token: JWT de acceso (corto). refreshToken: JWT para pedir un nuevo token de acceso.
 * expiraEn: milisegundos de vida del token de acceso.
 */
public record AuthResponse(
        String token,
        String refreshToken,
        String tipo,
        long expiraEn,
        UsuarioDTO usuario
) {
}
