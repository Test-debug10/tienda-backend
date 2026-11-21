package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.AuthResponse;
import cl.duoc.tienda.dto.LoginRequest;
import cl.duoc.tienda.dto.RefreshRequest;
import cl.duoc.tienda.dto.RegistroRequest;
import cl.duoc.tienda.dto.UsuarioDTO;
import cl.duoc.tienda.model.Usuario;
import cl.duoc.tienda.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, UsuarioService usuarioService,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    /** Verifica correo y contraseña (BCrypt) y entrega los tokens. */
    public AuthResponse login(LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, datos.password()));
        } catch (Exception ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return respuesta(usuarioService.buscarPorCorreo(correo));
    }

    /** Registra un cliente y lo deja con la sesión iniciada. */
    public AuthResponse registrar(RegistroRequest datos) {
        return respuesta(usuarioService.registrar(datos));
    }

    /**
     * Renueva la sesión: recibe un refresh token válido y entrega un token de acceso nuevo
     * (y un refresh nuevo). Se vuelve a leer el usuario desde la BD para usar su rol actual.
     */
    public AuthResponse renovar(RefreshRequest datos) {
        try {
            Claims claims = jwtService.validar(datos.refreshToken(), JwtService.TIPO_RENOVACION);
            return respuesta(usuarioService.buscarPorCorreo(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Token de renovación inválido o expirado");
        }
    }

    private AuthResponse respuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generarToken(usuario),
                jwtService.generarRefreshToken(usuario),
                "Bearer",
                jwtService.getExpiracionMs(),
                UsuarioDTO.desde(usuario)
        );
    }
}
