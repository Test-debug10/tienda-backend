package cl.duoc.tienda.util;

/**
 * Valida el RUN chileno con el algoritmo de módulo 11 (RF-009).
 * Acepta el formato sin puntos ni guion (ej: 19011022K) y también con ellos.
 */
public final class ValidadorRun {

    private ValidadorRun() {
    }

    /** Deja el RUN sin puntos, guion ni espacios y con la K en mayúscula. */
    public static String normalizar(String run) {
        if (run == null) {
            return "";
        }
        return run.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
    }

    public static boolean esValido(String run) {
        String limpio = normalizar(run);
        if (!limpio.matches("^\\d{7,8}[0-9K]$")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        return calcularDigitoVerificador(cuerpo) == dv;
    }

    public static char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return Character.forDigit(resto, 10);
    }
}
