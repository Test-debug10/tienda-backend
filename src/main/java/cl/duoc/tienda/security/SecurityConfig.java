package cl.duoc.tienda.security;

import cl.duoc.tienda.exception.ManejadorErrores;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Reglas de acceso de la API. La sesión es STATELESS: el servidor no guarda sesiones,
 * cada petición se autentica con su token JWT.
 *
 * Resumen de permisos:
 *  - Público: login, registro, renovar token, Swagger, ver productos y categorías.
 *  - Cualquier usuario autenticado: su perfil, cambiar contraseña y ver sus compras.
 *  - CLIENTE y ADMIN: comprar (el VENDEDOR no compra, solo revisa).
 *  - ADMIN y VENDEDOR: ver todas las órdenes y su detalle.
 *  - Solo ADMIN: crear/editar/eliminar productos, categorías, usuarios y cambiar estado de órdenes.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String API = "/api/v1";

    @Value("${app.cors.allowed-origins}")
    private String origenesPermitidos;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(UsuarioDetailsService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarios);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return provider::authenticate;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            ObjectMapper objectMapper) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // consola H2
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                escribirError(res, objectMapper, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión"))
                        .accessDeniedHandler((req, res, e) ->
                                escribirError(res, objectMapper, HttpStatus.FORBIDDEN, "No tienes permisos para esta acción")))
                .authorizeHttpRequests(auth -> auth
                        // Documentación y consola de BD
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Autenticación
                        .requestMatchers(HttpMethod.POST, API + "/auth/login", API + "/auth/registro", API + "/auth/refresh").permitAll()
                        .requestMatchers(API + "/auth/**").authenticated()

                        // Catálogo público (el reporte de stock crítico es interno)
                        .requestMatchers(HttpMethod.GET, API + "/productos/stock-critico").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, API + "/productos/**", API + "/categorias/**").permitAll()
                        .requestMatchers(API + "/productos/**", API + "/categorias/**").hasRole("ADMIN")

                        // Órdenes
                        .requestMatchers(HttpMethod.POST, API + "/ordenes").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/ordenes/mias").authenticated()
                        .requestMatchers(HttpMethod.GET, API + "/ordenes/**").hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(API + "/ordenes/**").hasRole("ADMIN")

                        // Usuarios
                        .requestMatchers(API + "/usuarios/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenesPermitidos.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void escribirError(HttpServletResponse res, ObjectMapper mapper, HttpStatus estado,
                                      String mensaje) throws IOException {
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), ManejadorErrores.cuerpo(estado, mensaje));
    }
}
