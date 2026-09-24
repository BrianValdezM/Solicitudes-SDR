package com.safedata.sdr.solicitudes.config;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.login.AutenticacionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacionService autenticacionService;

    public LoginSuccessHandler(UsuarioRepository usuarioRepository, AutenticacionService autenticacionService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacionService = autenticacionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow();

        autenticacionService.registrarLoginExitoso(usuario);

        HttpSession session = request.getSession();
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("nombreCompleto", usuario.getNombreCompleto());

        if (autenticacionService.contrasenaExpirada(usuario)) {
            session.setAttribute("2fa_verificado", false);
            getRedirectStrategy().sendRedirect(request, response, "/cambiar-contrasena?obligatorio");
            return;
        }

        if (usuario.isDosFactoresActivo()) {
            session.setAttribute("2fa_verificado", false);
            getRedirectStrategy().sendRedirect(request, response, "/verificar-2fa");
            return;
        }

        // No tiene 2FA activo. Dejarlo pasar pero quizá ofrecerle activarlo.
        session.setAttribute("2fa_verificado", true);

        if (autenticacionService.debeRecordarConfiguracion2fa(usuario)) {
            getRedirectStrategy().sendRedirect(request, response, "/recordatorio-2fa");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/solicitudes");
    }
}
