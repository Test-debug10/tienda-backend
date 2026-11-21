package cl.duoc.tienda.model;

/**
 * Roles del sistema. Spring Security los usa con el prefijo ROLE_ (ROLE_ADMIN, etc.).
 * ADMIN: acceso total. VENDEDOR: ve productos y órdenes. CLIENTE: solo la tienda.
 */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE
}
