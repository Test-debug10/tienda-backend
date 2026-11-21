package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.ProductoDTO;
import cl.duoc.tienda.dto.ProductoRequest;
import cl.duoc.tienda.exception.RecursoNoEncontradoException;
import cl.duoc.tienda.exception.ReglaNegocioException;
import cl.duoc.tienda.model.Producto;
import cl.duoc.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(Long categoriaId) {
        List<Producto> productos = categoriaId == null
                ? productoRepository.findAllByOrderByIdAsc()
                : productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
        return productos.stream().map(ProductoDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarOfertas() {
        return productoRepository.findEnOferta().stream().map(ProductoDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarStockCritico() {
        return productoRepository.findConStockCritico().stream().map(ProductoDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtener(Long id) {
        return ProductoDTO.desde(buscar(id));
    }

    @Transactional
    public ProductoDTO crear(ProductoRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase();
        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + codigo);
        }
        Producto producto = new Producto();
        copiarDatos(producto, datos);
        return ProductoDTO.desde(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoRequest datos) {
        Producto producto = buscar(id);
        String codigo = datos.codigo().trim().toUpperCase();
        if (!producto.getCodigo().equalsIgnoreCase(codigo) && productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con el código " + codigo);
        }
        copiarDatos(producto, datos);
        return ProductoDTO.desde(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.delete(buscar(id));
    }

    private void copiarDatos(Producto producto, ProductoRequest datos) {
        if (datos.precioOferta() != null && datos.precioOferta() >= datos.precio()) {
            throw new ReglaNegocioException("El precio de oferta debe ser menor al precio normal");
        }
        producto.setCodigo(datos.codigo().trim().toUpperCase());
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
        producto.setPrecioOferta(datos.precioOferta());
        producto.setStock(datos.stock());
        producto.setStockCritico(datos.stockCritico());
        producto.setImagen(datos.imagen());
        producto.setEmoji(datos.emoji());
        producto.setCategoria(categoriaService.buscar(datos.categoriaId()));
    }

    Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto " + id + " no encontrado"));
    }
}
