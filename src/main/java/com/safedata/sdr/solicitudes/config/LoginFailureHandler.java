package com.safedata.sdr.solicitudes.config;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.login.AutenticacionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacionService autenticacionService;

    public LoginFailureHandler(UsuarioRepository usuarioRepository, AutenticacionService autenticacionService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacionService = autenticacionService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {

        String correo = request.getParameter("username");
        String razon = "credenciales";

        if (correo != null) {
            Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
            if (usuario != null) {
                if (autenticacionService.cuentaBloqueada(usuario)) {
                    razon = "bloqueada";
                } else {
                    autenticacionService.registrarIntentoFallido(usuario);
                    razon = autenticacionService.cuentaBloqueada(usuario) ? "bloqueada" : "credenciales";
                }
            }
        }

        getRedirectStrategy().sendRedirect(request, response, "/login?error=" + razon);
    }
}
