package cl.duoc.tienda.controller;

import cl.duoc.tienda.dto.ProductoDTO;
import cl.duoc.tienda.dto.ProductoRequest;
import cl.duoc.tienda.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de productos. Lectura pública, escritura solo ADMIN.")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Público. Se puede filtrar por categoría.")
    public List<ProductoDTO> listar(@Parameter(description = "Id de categoría (opcional)")
                                    @RequestParam(required = false) Long categoriaId) {
        return productoService.listar(categoriaId);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Listar productos en oferta", description = "Público. Productos con precio de oferta menor al normal.")
    public List<ProductoDTO> ofertas() {
        return productoService.listarOfertas();
    }

    @GetMapping("/stock-critico")
    @Operation(summary = "Productos con stock crítico", description = "Productos cuyo stock es menor o igual al stock crítico.")
    public List<ProductoDTO> stockCritico() {
        return productoService.listarStockCritico();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto", description = "Público.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ProductoDTO obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear producto", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "201", description = "Producto creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "409", description = "Código duplicado")
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(datos));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar producto", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ProductoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return productoService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar producto", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
