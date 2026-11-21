package cl.duoc.tienda.controller;

import cl.duoc.tienda.dto.CambioEstadoRequest;
import cl.duoc.tienda.dto.OrdenDTO;
import cl.duoc.tienda.dto.OrdenRequest;
import cl.duoc.tienda.service.OrdenService;
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
@RequestMapping("/api/v1/ordenes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Órdenes", description = "Compras. Crear: cualquier usuario conectado. Ver todas: ADMIN y VENDEDOR. Modificar: ADMIN.")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @Operation(summary = "Crear orden (checkout)",
            description = "Valida stock, calcula el total con los precios de la BD y descuenta el stock.")
    @ApiResponse(responseCode = "201", description = "Compra realizada")
    @ApiResponse(responseCode = "400", description = "Datos de despacho inválidos o carrito vacío")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    public ResponseEntity<OrdenDTO> crear(@Valid @RequestBody OrdenRequest datos, Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crear(datos, autenticacion.getName()));
    }

    @GetMapping("/mias")
    @Operation(summary = "Mis compras", description = "Órdenes del usuario conectado.")
    public List<OrdenDTO> mias(Authentication autenticacion) {
        return ordenService.listarDeUsuario(autenticacion.getName());
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes", description = "ADMIN y VENDEDOR.")
    @ApiResponse(responseCode = "403", description = "El rol no tiene permiso")
    public List<OrdenDTO> listar() {
        return ordenService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una orden", description = "ADMIN y VENDEDOR.")
    @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    public OrdenDTO obtener(@PathVariable Long id) {
        return ordenService.obtener(id);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de una orden", description = "Solo ADMIN. Si se cancela, se devuelve el stock.")
    public OrdenDTO cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest datos) {
        return ordenService.cambiarEstado(id, datos.estado());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar orden", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "204", description = "Orden eliminada")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
