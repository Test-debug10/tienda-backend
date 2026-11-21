package cl.duoc.tienda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración: levantan el backend completo con H2 en memoria
 * y verifican login, JWT y permisos por rol.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    private JsonNode login(String correo, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(cuerpo);
    }

    private String token(String correo, String password) throws Exception {
        return "Bearer " + login(correo, password).get("token").asText();
    }

    private static final String PRODUCTO_NUEVO = """
            {"codigo":"TST-001","nombre":"Producto test","descripcion":"x","precio":1000,
             "stock":5,"stockCritico":1,"categoriaId":1}
            """;

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    }

    @Test
    void loginDevuelveTokensYRol() throws Exception {
        JsonNode respuesta = login("admin@duoc.cl", "Admin1234@");
        org.junit.jupiter.api.Assertions.assertFalse(respuesta.get("token").asText().isBlank());
        org.junit.jupiter.api.Assertions.assertFalse(respuesta.get("refreshToken").asText().isBlank());
        org.junit.jupiter.api.Assertions.assertEquals("ADMIN", respuesta.get("usuario").get("rol").asText());
    }

    @Test
    void loginConClaveIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearProductoSinTokenDa401() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeCrearProductosNiVerOrdenes() throws Exception {
        String token = token("cliente@gmail.com", "Cliente123@");
        mvc.perform(post("/api/v1/productos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorVeOrdenesPeroNoUsuariosNiEditaProductos() throws Exception {
        String token = token("vendedor@duoc.cl", "Vendedor123@");
        mvc.perform(get("/api/v1/ordenes").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/productos/1").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCrudDeProductos() throws Exception {
        String token = token("admin@duoc.cl", "Admin1234@");
        String creado = mvc.perform(post("/api/v1/productos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_NUEVO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creado).get("id").asLong();

        mvc.perform(delete("/api/v1/productos/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void tokenAlteradoDa401() throws Exception {
        // Un cliente edita el payload de su token para ponerse rol ADMIN sin volver a firmarlo
        String jwt = login("cliente@gmail.com", "Cliente123@").get("token").asText();
        String[] partes = jwt.split("\\.");
        java.util.Base64.Decoder dec = java.util.Base64.getUrlDecoder();
        java.util.Base64.Encoder enc = java.util.Base64.getUrlEncoder().withoutPadding();
        String payload = new String(dec.decode(partes[1]), java.nio.charset.StandardCharsets.UTF_8)
                .replace("\"CLIENTE\"", "\"ADMIN\"");
        String falso = partes[0] + "." + enc.encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                + "." + partes[2];

        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEntregaNuevoTokenYNoSirveComoAcceso() throws Exception {
        String refresh = login("cliente@gmail.com", "Cliente123@").get("refreshToken").asText();

        // El refresh token no sirve para entrar a recursos protegidos
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registroValidaRunYCorreo() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"run":"123456789","nombre":"A","apellidos":"B","correo":"a@hotmail.com",
                                 "password":"Clave1234","region":"R","comuna":"C","direccion":"D"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteCompraYVeSusOrdenes() throws Exception {
        String token = token("cliente@gmail.com", "Cliente123@");
        mvc.perform(post("/api/v1/ordenes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Pedro","apellidos":"González","correo":"cliente@gmail.com",
                                 "calle":"Calle 1","region":"Metropolitana de Santiago","comuna":"Santiago",
                                 "items":[{"productoId":2,"cantidad":1}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(29990));
        mvc.perform(get("/api/v1/ordenes/mias").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detalles[0].cantidad").value(1));
    }
}
