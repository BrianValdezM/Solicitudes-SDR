package com.safedata.sdr.solicitudes.service.login;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.model.login.TokenRecuperacion;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.repository.login.TokenRecuperacionRepository;
import com.safedata.sdr.solicitudes.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class RecuperacionService {

    private static final int MINUTOS_VIGENCIA_TOKEN = 30;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final AutenticacionService autenticacionService;
    private final EmailService emailService;

    @Value("${app.url-base:http://localhost:8080}")
    private String urlBase;

    public RecuperacionService(UsuarioRepository usuarioRepository,
                                TokenRecuperacionRepository tokenRepository,
                                AutenticacionService autenticacionService,
                                EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.autenticacionService = autenticacionService;
        this.emailService = emailService;
    }

    @Transactional
    public Optional<String> solicitarRecuperacion(String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoIgnoreCase(correo);
        if (usuarioOpt.isEmpty()) return Optional.empty();

        Usuario usuario = usuarioOpt.get();
        String token = generarTokenAleatorio();

        TokenRecuperacion tokenRecuperacion = new TokenRecuperacion();
        tokenRecuperacion.setUsuario(usuario);
        tokenRecuperacion.setToken(token);
        tokenRecuperacion.setFechaExpira(LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_TOKEN));
        tokenRepository.save(tokenRecuperacion);

        String enlace = urlBase + "/restablecer-contrasena?token=" + token;
        emailService.enviarRecuperacionContrasena(
                usuario.getCorreo(), usuario.getNombreCompleto(), enlace);

        return Optional.of(token);
    }

    public Optional<TokenRecuperacion> validarToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(TokenRecuperacion::estaVigente);
    }

    @Transactional
    public boolean restablecerContrasena(String token, String nuevaContrasena) {
        Optional<TokenRecuperacion> tokenOpt = validarToken(token);
        if (tokenOpt.isEmpty()) return false;

        TokenRecuperacion tokenRecuperacion = tokenOpt.get();
        Usuario usuario = tokenRecuperacion.getUsuario();

        autenticacionService.cambiarContrasena(usuario, nuevaContrasena);

        tokenRecuperacion.setUsado(true);
        tokenRepository.save(tokenRecuperacion);
        return true;
    }

    private String generarTokenAleatorio() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}