package cl.duoc.tienda.exception;

/** Se lanza cuando una operación rompe una regla del negocio (stock, duplicados, etc.). Responde 409. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
