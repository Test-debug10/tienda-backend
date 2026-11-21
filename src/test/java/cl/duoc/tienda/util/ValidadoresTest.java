package cl.duoc.tienda.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadoresTest {

    @Test
    void runValidoConYSinFormato() {
        assertTrue(ValidadorRun.esValido("123456785"));
        assertTrue(ValidadorRun.esValido("12.345.678-5"));
        assertTrue(ValidadorRun.esValido("111111111"));
    }

    @Test
    void runConDigitoVerificadorKEnMinuscula() {
        // 10000013-K es válido
        assertEquals('K', ValidadorRun.calcularDigitoVerificador("10000013"));
        assertTrue(ValidadorRun.esValido("10000013k"));
    }

    @Test
    void runInvalido() {
        assertFalse(ValidadorRun.esValido("123456789"));
        assertFalse(ValidadorRun.esValido("abc"));
        assertFalse(ValidadorRun.esValido(""));
        assertFalse(ValidadorRun.esValido(null));
    }

    @Test
    void correoSoloDominiosPermitidos() {
        assertTrue(ValidadorCorreo.esPermitido("alumno@duoc.cl"));
        assertTrue(ValidadorCorreo.esPermitido("profe@profesor.duoc.cl"));
        assertTrue(ValidadorCorreo.esPermitido("Persona@Gmail.com"));
        assertFalse(ValidadorCorreo.esPermitido("persona@hotmail.com"));
        assertFalse(ValidadorCorreo.esPermitido("sin-arroba.gmail.com"));
    }
}
