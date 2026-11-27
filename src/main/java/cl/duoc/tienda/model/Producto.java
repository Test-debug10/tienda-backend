package cl.duoc.tienda.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "productos")
// Validaciones en la propia base de datos: aunque alguien salte el backend, PostgreSQL no acepta estos valores
@Check(name = "ck_productos_precio", constraints = "precio > 0")
@Check(name = "ck_productos_precio_oferta", constraints = "precio_oferta IS NULL OR precio_oferta > 0")
@Check(name = "ck_productos_stock", constraints = "stock >= 0")
@Check(name = "ck_productos_stock_critico", constraints = "stock_critico >= 0")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    /** Precio rebajado. Si es null el producto no está en oferta. */
    private Integer precioOferta;

    @Column(nullable = false)
    private Integer stock;

    /** Cuando el stock llega a este valor se avisa en el panel de administración. */
    @Column(nullable = false)
    private Integer stockCritico;

    @Column(length = 255)
    private String imagen;

    @Column(length = 16)
    private String emoji;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public Producto() {
    }

    /** Precio que realmente se cobra: el de oferta si existe y es menor al normal. */
    public int precioFinal() {
        if (precioOferta != null && precioOferta > 0 && precioOferta < precio) {
            return precioOferta;
        }
        return precio;
    }

    public boolean enOferta() {
        return precioFinal() < precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }
    public Integer getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(Integer precioOferta) { this.precioOferta = precioOferta; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getStockCritico() { return stockCritico; }
    public void setStockCritico(Integer stockCritico) { this.stockCritico = stockCritico; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
