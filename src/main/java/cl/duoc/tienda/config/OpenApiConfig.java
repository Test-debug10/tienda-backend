package cl.duoc.tienda.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger. Disponible en http://localhost:8080/swagger-ui.html
 * El botón "Authorize" permite pegar el token JWT para probar los endpoints protegidos.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API Tienda Gamer",
        version = "v1",
        description = "API REST de la tienda online (DSY1104). Autenticación con JWT y roles ADMIN, VENDEDOR y CLIENTE."
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
