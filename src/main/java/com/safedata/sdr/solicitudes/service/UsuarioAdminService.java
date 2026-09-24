package com.safedata.sdr.solicitudes.service;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.repository.login.HistorialContrasenaRepository;
import com.safedata.sdr.solicitudes.repository.login.TokenRecuperacionRepository;
import com.safedata.sdr.solicitudes.util.PasswordTemporalGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final HistorialContrasenaRepository historialContrasenaRepository;

    public UsuarioAdminService(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService,
                               TokenRecuperacionRepository tokenRecuperacionRepository,
                               HistorialContrasenaRepository historialContrasenaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.historialContrasenaRepository = historialContrasenaRepository;
    }

    @Transactional
    public Usuario crearUsuario(String nombre, String apellidos, String correo,
                                String telefono, String idCliente, String rol) {

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }

        String passwordTemporal = PasswordTemporalGenerator.generar();

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo.toLowerCase().trim());
        usuario.setTelefono(telefono);
        usuario.setIdCliente(idCliente);          // ← nuevo
        usuario.setRol(rol == null || rol.isBlank() ? "USUARIO" : rol);
        usuario.setActivo(true);
        usuario.setContrasenaHash(passwordEncoder.encode(passwordTemporal));
        usuario.setFechaCambioContrasena(LocalDateTime.now());
        usuario.setDebeCambiarContrasena(true);
        usuarioRepository.save(usuario);

        emailService.enviarContrasenaTemporal(
                usuario.getCorreo(), usuario.getNombreCompleto(), passwordTemporal);

        return usuario;
    }
    
    @Transactional
    public Usuario actualizarUsuario(Integer id, String nombre, String apellidos,
                                      String correo, String telefono,
                                      String idCliente, String rol,
                                      boolean resetearContrasena) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Validar que el correo no lo tenga OTRO usuario
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro usuario con ese correo.");
            }
        });

        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo.toLowerCase().trim());
        usuario.setTelefono(telefono);
        usuario.setIdCliente(idCliente);
        usuario.setRol(rol == null || rol.isBlank() ? "USUARIO" : rol);

        String passwordTemporal = null;
        if (resetearContrasena) {
            // Invalidar tokens de recuperación vigentes
            tokenRecuperacionRepository.deleteAll(
                    tokenRecuperacionRepository.findByUsuario(usuario));

            passwordTemporal = PasswordTemporalGenerator.generar();
            usuario.setContrasenaHash(passwordEncoder.encode(passwordTemporal));
            usuario.setFechaCambioContrasena(LocalDateTime.now());
            usuario.setDebeCambiarContrasena(true);
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        Usuario guardado = usuarioRepository.save(usuario);

        if (resetearContrasena) {
            emailService.enviarContrasenaRestablecida(
                    guardado.getCorreo(), guardado.getNombreCompleto(), passwordTemporal);
        }

        return guardado;
    }
    
    @Transactional
    public void eliminarUsuario(Integer id, Integer idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Protección: no borrar al último admin activo
        if ("ADMIN".equals(usuario.getRol())
                && usuario.isActivo()
                && usuarioRepository.countByRolAndActivoTrue("ADMIN") <= 1) {
            throw new IllegalArgumentException("No puedes eliminar al último administrador activo.");
        }

        // Limpiar dependencias (FK)
        tokenRecuperacionRepository.deleteAll(tokenRecuperacionRepository.findByUsuario(usuario));
        historialContrasenaRepository.deleteAll(historialContrasenaRepository.findByUsuario(usuario));

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void cambiarEstado(Integer id, Integer idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Si va a desactivar a un admin, verificar que no sea el último activo
        if (usuario.isActivo()
                && "ADMIN".equals(usuario.getRol())
                && usuarioRepository.countByRolAndActivoTrue("ADMIN") <= 1) {
            throw new IllegalArgumentException("No puedes desactivar al último administrador activo.");
        }

        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);
    }
}