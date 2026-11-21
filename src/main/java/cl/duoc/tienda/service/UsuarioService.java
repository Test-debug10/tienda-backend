package cl.duoc.tienda.service;

import cl.duoc.tienda.dto.CambioPasswordRequest;
import cl.duoc.tienda.dto.RegistroRequest;
import cl.duoc.tienda.dto.UsuarioDTO;
import cl.duoc.tienda.dto.UsuarioRequest;
import cl.duoc.tienda.exception.RecursoNoEncontradoException;
import cl.duoc.tienda.exception.ReglaNegocioException;
import cl.duoc.tienda.model.Rol;
import cl.duoc.tienda.model.Usuario;
import cl.duoc.tienda.repository.OrdenRepository;
import cl.duoc.tienda.repository.UsuarioRepository;
import cl.duoc.tienda.util.ValidadorCorreo;
import cl.duoc.tienda.util.ValidadorRun;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, OrdenRepository ordenRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.ordenRepository = ordenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Registro público: siempre crea un CLIENTE. */
    @Transactional
    public Usuario registrar(RegistroRequest datos) {
        Usuario usuario = new Usuario();
        usuario.setRun(validarRunDisponible(datos.run(), null));
        usuario.setCorreo(validarCorreoDisponible(datos.correo(), null));
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setPassword(passwordEncoder.encode(datos.password()));
        usuario.setRol(Rol.CLIENTE);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion().trim());
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAllByOrderByIdAsc().stream().map(UsuarioDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtener(Long id) {
        return UsuarioDTO.desde(buscar(id));
    }

    @Transactional
    public UsuarioDTO crear(UsuarioRequest datos) {
        if (datos.password() == null || datos.password().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        Usuario usuario = new Usuario();
        usuario.setPassword(passwordEncoder.encode(datos.password()));
        copiarDatos(usuario, datos, null);
        return UsuarioDTO.desde(usuarioRepository.save(usuario));
    }

    /**
     * Edita un usuario. Reglas: un admin no puede quitarse a sí mismo el rol ADMIN
     * y siempre debe quedar al menos un administrador.
     */
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioRequest datos, String correoSolicitante) {
        Usuario usuario = buscar(id);
        if (usuario.getRol() == Rol.ADMIN && datos.rol() != Rol.ADMIN) {
            if (usuario.getCorreo().equalsIgnoreCase(correoSolicitante)) {
                throw new ReglaNegocioException("No puedes quitarte a ti mismo el rol de administrador");
            }
            if (contarAdministradores() <= 1) {
                throw new ReglaNegocioException("Debe existir al menos un administrador");
            }
        }
        if (datos.password() != null && !datos.password().isBlank()) {
            if (datos.password().length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(datos.password()));
        }
        copiarDatos(usuario, datos, id);
        return UsuarioDTO.desde(usuario);
    }

    @Transactional
    public void eliminar(Long id, String correoSolicitante) {
        Usuario usuario = buscar(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoSolicitante)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        if (usuario.getRol() == Rol.ADMIN && contarAdministradores() <= 1) {
            throw new ReglaNegocioException("Debe existir al menos un administrador");
        }
        if (ordenRepository.existsByUsuarioId(id)) {
            throw new ReglaNegocioException("No se puede eliminar: el usuario tiene órdenes registradas");
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void cambiarPassword(String correo, CambioPasswordRequest datos) {
        Usuario usuario = buscarPorCorreo(correo);
        if (!passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        usuario.setPassword(passwordEncoder.encode(datos.nuevaPassword()));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void copiarDatos(Usuario usuario, UsuarioRequest datos, Long idActual) {
        usuario.setRun(validarRunDisponible(datos.run(), idActual));
        usuario.setCorreo(validarCorreoDisponible(datos.correo(), idActual));
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setRol(datos.rol());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());
    }

    private String validarRunDisponible(String run, Long idActual) {
        if (!ValidadorRun.esValido(run)) {
            throw new IllegalArgumentException("El RUN no es válido");
        }
        String limpio = ValidadorRun.normalizar(run);
        boolean ocupado = usuarioRepository.findAll().stream()
                .anyMatch(u -> u.getRun().equals(limpio) && !u.getId().equals(idActual));
        if (ocupado) {
            throw new ReglaNegocioException("El RUN ya está registrado");
        }
        return limpio;
    }

    private String validarCorreoDisponible(String correo, Long idActual) {
        if (!ValidadorCorreo.esPermitido(correo)) {
            throw new IllegalArgumentException("Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com");
        }
        String limpio = correo.trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(limpio)
                .filter(u -> !u.getId().equals(idActual))
                .ifPresent(u -> {
                    throw new ReglaNegocioException("El correo ya está registrado");
                });
        return limpio;
    }

    private long contarAdministradores() {
        return usuarioRepository.findAll().stream().filter(u -> u.getRol() == Rol.ADMIN).count();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + id + " no encontrado"));
    }
}
