package cl.duoc.tienda.controller;

import cl.duoc.tienda.dto.*;
import cl.duoc.tienda.service.AuthService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida correo y contraseña y devuelve el token de acceso y el de renovación.")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    public AuthResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar cliente", description = "Crea una cuenta con rol CLIENTE y deja la sesión iniciada.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (RUN, correo, contraseña)")
    @ApiResponse(responseCode = "409", description = "RUN o correo ya registrado")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(datos));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Recibe el refresh token y entrega un token de acceso nuevo.")
    @ApiResponse(responseCode = "200", description = "Token renovado")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest datos) {
        return authService.renovar(datos);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Perfil del usuario conectado", description = "Sirve para que el frontend confirme que el token sigue siendo válido.")
    public UsuarioDTO me(Authentication autenticacion) {
        return UsuarioDTO.desde(usuarioService.buscarPorCorreo(autenticacion.getName()));
    }

    @PutMapping("/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar mi contraseña")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "400", description = "La contraseña actual no coincide")
    public Map<String, String> cambiarPassword(@Valid @RequestBody CambioPasswordRequest datos,
                                               Authentication autenticacion) {
        usuarioService.cambiarPassword(autenticacion.getName(), datos);
        return Map.of("mensaje", "Contraseña actualizada");
    }
}
