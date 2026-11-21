package cl.duoc.tienda.controller;

import cl.duoc.tienda.dto.UsuarioDTO;
import cl.duoc.tienda.dto.UsuarioRequest;
import cl.duoc.tienda.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Mantenedor de usuarios y roles. Solo ADMIN.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un usuario")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public UsuarioDTO obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario con rol")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "409", description = "RUN o correo duplicado")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Contraseña vacía = se conserva la actual.")
    public UsuarioDTO actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos,
                                 Authentication autenticacion) {
        return usuarioService.actualizar(id, datos, autenticacion.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado")
    @ApiResponse(responseCode = "409", description = "No se puede eliminar (propia cuenta, último admin o tiene órdenes)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        usuarioService.eliminar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
