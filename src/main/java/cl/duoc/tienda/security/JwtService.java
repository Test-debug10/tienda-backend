package cl.duoc.tienda.security;

import cl.duoc.tienda.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Genera y verifica los tokens JWT.
 * - Token de acceso: dura poco (30 min) y va en cada petición (Authorization: Bearer ...).
 * - Token de renovación (refresh): dura más (7 días) y solo sirve para pedir un nuevo token de acceso.
 * Ambos se firman con HMAC-SHA256 usando una clave secreta que solo conoce el servidor.
 */
@Service
public class JwtService {

    public static final String TIPO_ACCESO = "access";
    public static final String TIPO_RENOVACION = "refresh";

    private final SecretKey claveFirma;
    private final long expiracionMs;
    private final long expiracionRefreshMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs,
                      @Value("${app.jwt.refresh-expiration-ms}") long expiracionRefreshMs) {
        this.claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracionMs = expiracionMs;
        this.expiracionRefreshMs = expiracionRefreshMs;
    }

    public String generarToken(Usuario usuario) {
        return construir(usuario, TIPO_ACCESO, expiracionMs);
    }

    public String generarRefreshToken(Usuario usuario) {
        return construir(usuario, TIPO_RENOVACION, expiracionRefreshMs);
    }

    private String construir(Usuario usuario, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .claim("uid", usuario.getId())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(claveFirma)
                .compact();
    }

    /**
     * Verifica firma y expiración. Si el token fue alterado o venció, jjwt lanza JwtException.
     */
    public Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(claveFirma)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Devuelve los claims solo si el token es válido y del tipo esperado. */
    public Claims validar(String token, String tipoEsperado) {
        Claims claims = leerClaims(token);
        if (!tipoEsperado.equals(claims.get("tipo", String.class))) {
            throw new JwtException("El token no es de tipo " + tipoEsperado);
        }
        return claims;
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }
}
