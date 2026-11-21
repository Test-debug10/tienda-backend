package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.CategoriaDTO;
import cl.duoc.tienda.dto.CategoriaRequest;
import cl.duoc.tienda.exception.RecursoNoEncontradoException;
import cl.duoc.tienda.exception.ReglaNegocioException;
import cl.duoc.tienda.model.Categoria;
import cl.duoc.tienda.repository.CategoriaRepository;
import cl.duoc.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(CategoriaDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtener(Long id) {
        return CategoriaDTO.desde(buscar(id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaRequest datos) {
        String nombre = datos.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría llamada " + nombre);
        }
        return CategoriaDTO.desde(categoriaRepository.save(new Categoria(nombre, datos.descripcion())));
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaRequest datos) {
        Categoria categoria = buscar(id);
        String nombre = datos.nombre().trim();
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ReglaNegocioException("Ya existe una categoría llamada " + nombre);
                });
        categoria.setNombre(nombre);
        categoria.setDescripcion(datos.descripcion());
        return CategoriaDTO.desde(categoria);
    }

    /** No se puede borrar una categoría que todavía tiene productos. */
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar: la categoría tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría " + id + " no encontrada"));
    }
}
