package com.safedata.sdr.solicitudes.service.login;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.model.login.HistorialContrasena;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.repository.login.HistorialContrasenaRepository;
import com.safedata.sdr.solicitudes.util.TotpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AutenticacionService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final int DIAS_RECORDATORIO_2FA = 7;

    private final UsuarioRepository usuarioRepository;
    private final HistorialContrasenaRepository historialContrasenaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password.expiracion-dias:90}")
    private int diasExpiracionContrasena;

    @Value("${app.2fa.issuer:Portal de Solicitudes}")
    private String emisor2fa;

    public AutenticacionService(UsuarioRepository usuarioRepository,
                                 HistorialContrasenaRepository historialContrasenaRepository,
                                 PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.historialContrasenaRepository = historialContrasenaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean cuentaBloqueada(Usuario usuario) {
        return usuario.getBloqueadoHasta() != null
                && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void registrarIntentoFallido(Usuario usuario) {
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
        if (usuario.getIntentosFallidos() >= MAX_INTENTOS_FALLIDOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarLoginExitoso(Usuario usuario) {
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /** true si ya pasaron más de app.password.expiracion-dias desde el último cambio. */
    public boolean contrasenaExpirada(Usuario usuario) {
        if (usuario.isDebeCambiarContrasena()) return true;
        long dias = ChronoUnit.DAYS.between(usuario.getFechaCambioContrasena(), LocalDateTime.now());
        return dias >= diasExpiracionContrasena;
    }

    @Transactional
    public void cambiarContrasena(Usuario usuario, String nuevaContrasenaPlano) {
        // Guarda el hash anterior en el historial antes de sobrescribir
        HistorialContrasena registro = new HistorialContrasena();
        registro.setUsuario(usuario);
        registro.setContrasenaHash(usuario.getContrasenaHash());
        historialContrasenaRepository.save(registro);

        usuario.setContrasenaHash(passwordEncoder.encode(nuevaContrasenaPlano));
        usuario.setFechaCambioContrasena(LocalDateTime.now());
        usuario.setDebeCambiarContrasena(false);
        usuarioRepository.save(usuario);
    }

    // ---- 2FA ----

    @Transactional
    public String iniciarConfiguracion2fa(Usuario usuario) {
        String secreto = TotpUtil.generarSecreto();
        usuario.setSecreto2fa(secreto);
        usuario.setDosFactoresActivo(false); // se activa hasta confirmar el primer código
        usuarioRepository.save(usuario);
        return TotpUtil.generarUrlOtpAuth(secreto, usuario.getCorreo(), emisor2fa);
    }

    @Transactional
    public boolean confirmarActivacion2fa(Usuario usuario, String codigo) {
        if (usuario.getSecreto2fa() == null) return false;
        boolean valido = TotpUtil.validarCodigo(usuario.getSecreto2fa(), codigo);
        if (valido) {
            usuario.setDosFactoresActivo(true);
            usuarioRepository.save(usuario);
        }
        return valido;
    }

    public boolean validarCodigo2fa(Usuario usuario, String codigo) {
        if (usuario.getSecreto2fa() == null) return false;
        return TotpUtil.validarCodigo(usuario.getSecreto2fa(), codigo);
    }
    
    public boolean debeRecordarConfiguracion2fa(Usuario usuario) {
        if (usuario.isDosFactoresActivo()) return false;
        if (usuario.getFechaUltimoRecordatorio2fa() == null) return true;
        return usuario.getFechaUltimoRecordatorio2fa()
                .plusDays(DIAS_RECORDATORIO_2FA)
                .isBefore(LocalDateTime.now());
    }

    @Transactional
    public void registrarRecordatorio2fa(Usuario usuario) {
        usuario.setFechaUltimoRecordatorio2fa(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
    
    public String generarUrlOtpAuth(Usuario usuario) {
        return TotpUtil.generarUrlOtpAuth(
                usuario.getSecreto2fa(), usuario.getCorreo(), emisor2fa);
    }
}
