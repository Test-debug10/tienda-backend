package cl.duoc.tienda.config;

import cl.duoc.tienda.model.Categoria;
import cl.duoc.tienda.model.Producto;
import cl.duoc.tienda.model.Rol;
import cl.duoc.tienda.model.Usuario;
import cl.duoc.tienda.repository.CategoriaRepository;
import cl.duoc.tienda.repository.ProductoRepository;
import cl.duoc.tienda.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Carga datos de ejemplo la primera vez que se levanta el backend (si las tablas están vacías):
 * categorías, el catálogo de productos y un usuario por cada rol.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            crearUsuario("111111111", "Admin", "Tienda", "admin@duoc.cl", "Admin1234@", Rol.ADMIN);
            crearUsuario("222222222", "Valentina", "Rojas Soto", "vendedor@duoc.cl", "Vendedor123@", Rol.VENDEDOR);
            crearUsuario("123456785", "Pedro", "González Muñoz", "cliente@gmail.com", "Cliente123@", Rol.CLIENTE);
            log.info("Usuarios iniciales creados: admin@duoc.cl, vendedor@duoc.cl, cliente@gmail.com");
        }

        if (productoRepository.count() == 0) {
            Map<String, Categoria> categorias = new HashMap<>();
            for (String[] c : new String[][]{
                    {"Periféricos", "Teclados, mouse, audífonos y más"},
                    {"Monitores", "Pantallas para jugar y trabajar"},
                    {"Accesorios", "Sillas, mousepads y complementos"},
                    {"Componentes Gamers", "Partes para armar o mejorar tu PC"},
                    {"Computadores", "Gabinetes y equipos"}
            }) {
                Categoria categoria = categoriaRepository.findByNombreIgnoreCase(c[0])
                        .orElseGet(() -> categoriaRepository.save(new Categoria(c[0], c[1])));
                categorias.put(c[0], categoria);
            }

            producto("PER-001", "Teclado Mecánico RGB", 49990, 39990, 25, "⌨️", "/img/teclado-mecanico.webp", categorias.get("Periféricos"),
                    "Teclado mecánico con iluminación RGB, ideal para jugar y escribir con respuesta precisa.");
            producto("PER-002", "Mouse Gamer Pro", 29990, null, 40, "🖱️", "/img/mouse-gamer-pro.webp", categorias.get("Periféricos"),
                    "Mouse gamer diseñado para ofrecer control preciso y comodidad durante largas sesiones.");
            producto("PER-003", "Audífonos Inalámbricos", 39990, 32990, 18, "🎧", "/img/audifonos-inalambricos.webp", categorias.get("Periféricos"),
                    "Audífonos inalámbricos para disfrutar de audio envolvente y moverte con libertad.");
            producto("MON-001", "Monitor Gamer 24\" 144 Hz", 159990, 139990, 8, "🖥️", "/img/monitor-gamer.webp", categorias.get("Monitores"),
                    "Monitor gamer de 24 pulgadas y 144 Hz para una imagen fluida en tus juegos.");
            producto("ACC-001", "Silla Gamer Ergonómica", 129990, null, 6, "💺", "/img/silla-gamer.webp", categorias.get("Accesorios"),
                    "Silla ergonómica que brinda soporte y comodidad para jugar o trabajar.");
            producto("ACC-002", "Mousepad XL", 12990, 9990, 60, "🎯", "/img/mousepad-xl.webp", categorias.get("Accesorios"),
                    "Superficie amplia para mover el mouse con comodidad y mantenerlo bajo control.");
            producto("PER-004", "Micrófono Streaming", 44990, null, 12, "🎙️", "/img/microfono-usb.webp", categorias.get("Periféricos"),
                    "Micrófono USB pensado para streaming, videollamadas y grabaciones con voz clara.");
            producto("PER-005", "Control Inalámbrico", 34990, null, 30, "🎮", "/img/control-inalambrico.webp", categorias.get("Periféricos"),
                    "Control inalámbrico para jugar con comodidad y libertad de movimiento.");
            producto("PER-006", "Webcam Full 4K", 39990, null, 3, "📷", "", categorias.get("Periféricos"),
                    "Webcam 4K para videollamadas, transmisiones y creación de contenido.");
            producto("COM-001", "SSD NVMe M.2 1TB Kingston NV3", 64990, 54990, 20, "💾", "/img/ssd-nvme-1tb.webp", categorias.get("Componentes Gamers"),
                    "Unidad SSD NVMe M.2 de 1 TB para acelerar el inicio del sistema y la carga de aplicaciones.");
            producto("COM-002", "Tarjeta de Video NVIDIA RTX 3050 8GB", 349990, null, 4, "📟", "/img/rtx-3050.webp", categorias.get("Componentes Gamers"),
                    "Tarjeta gráfica NVIDIA RTX 3050 con 8 GB de memoria para jugar y crear contenido.");
            producto("COM-003", "Memoria RAM DDR4 16GB (2x8GB) 3200MHz", 42990, null, 22, "⚡", "/img/ram-ddr4-16gb.webp", categorias.get("Componentes Gamers"),
                    "Kit de memoria DDR4 de 16 GB (2 x 8 GB) a 3200 MHz para mejorar la capacidad de respuesta del equipo.");
            producto("PC-001", "Gabinete Gamer ATX Vidrio Templado", 54990, null, 10, "📦", "/img/gabinete-atx.webp", categorias.get("Computadores"),
                    "Gabinete ATX con panel de vidrio templado para exhibir y organizar los componentes del PC.");
            producto("COM-004", "Cooler Líquido 240mm RGB", 69990, 59990, 9, "❄️", "/img/cooler-liquido.webp", categorias.get("Componentes Gamers"),
                    "Sistema de refrigeración líquida de 240 mm con iluminación RGB para mantener el procesador refrigerado.");
            producto("COM-005", "Fuente de Poder 650W 80 Plus Bronze", 52990, null, 15, "🔌", "/img/fuente-650w.webp", categorias.get("Componentes Gamers"),
                    "Fuente de poder de 650 W con certificación 80 Plus Bronze para alimentar tu equipo.");
            log.info("Catálogo inicial cargado: {} productos", productoRepository.count());
        }
    }

    private void crearUsuario(String run, String nombre, String apellidos, String correo, String password, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setRegion("Metropolitana de Santiago");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Av. Libertador Bernardo O'Higgins 1234");
        usuarioRepository.save(usuario);
    }

    private void producto(String codigo, String nombre, int precio, Integer precioOferta, int stock, String emoji,
                          String imagen, Categoria categoria, String descripcion) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setPrecioOferta(precioOferta);
        p.setStock(stock);
        p.setStockCritico(5);
        p.setEmoji(emoji);
        p.setImagen(imagen);
        p.setCategoria(categoria);
        p.setDescripcion(descripcion);
        productoRepository.save(p);
    }
}
