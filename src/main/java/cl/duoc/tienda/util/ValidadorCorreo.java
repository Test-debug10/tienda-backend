package cl.duoc.tienda.util;

import java.util.List;

/** Solo se permiten correos de los dominios definidos en el requerimiento RF-002. */
public final class ValidadorCorreo {

    public static final List<String> DOMINIOS_PERMITIDOS = List.of("@duoc.cl", "@profesor.duoc.cl", "@gmail.com");

    private ValidadorCorreo() {
    }

    public static boolean esPermitido(String correo) {
        if (correo == null) {
            return false;
        }
        String valor = correo.trim().toLowerCase();
        if (!valor.matches("^[^@\\s]+@[^@\\s]+$")) {
            return false;
        }
        return DOMINIOS_PERMITIDOS.stream().anyMatch(valor::endsWith);
    }
}
