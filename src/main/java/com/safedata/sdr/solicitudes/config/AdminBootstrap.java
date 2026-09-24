package com.safedata.sdr.solicitudes.config;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.correo:}")   private String correoAdmin;
    @Value("${app.admin.password:}") private String passwordAdmin;

    public AdminBootstrap(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (correoAdmin.isBlank() || passwordAdmin.isBlank()) {
            log.warn("No se configuró app.admin.correo/password. No se crea admin inicial.");
            return;
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correoAdmin)) {
            log.info("Admin {} ya existe, no se crea de nuevo.", correoAdmin);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setApellidos("Sistema");
        admin.setCorreo(correoAdmin.toLowerCase());
        admin.setRol("ADMIN");
        admin.setActivo(true);
        admin.setContrasenaHash(passwordEncoder.encode(passwordAdmin));
        admin.setFechaCambioContrasena(LocalDateTime.now());
        admin.setDebeCambiarContrasena(true);   // ← forzamos el flujo completo
        usuarioRepository.save(admin);

        log.info("Admin inicial {} creado. Al primer login se pedirá cambiar contraseña.", correoAdmin);
    }
}