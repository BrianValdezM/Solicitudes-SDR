package com.safedata.sdr.solicitudes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Aunque el usuario ya tenga una sesión autenticada por Spring Security,
 * no lo dejamos usar el resto del sistema hasta que confirme el código
 * de 2FA (cuando lo tiene activo) o cambie su contraseña si ya expiró.
 * Evita que alguien brinque directo a una URL saltándose el segundo factor.
 */
public class DosFactoresFilter extends OncePerRequestFilter {

	private static final Set<String> RUTAS_PERMITIDAS = Set.of(
	        "/login", "/logout", "/verificar-2fa", "/cambiar-contrasena",
	        "/olvide-contrasena", "/restablecer-contrasena",
	        "/recordatorio-2fa",          // ← nuevo
	        "/css", "/js", "/img"
	);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        
        boolean esRutaPermitida = RUTAS_PERMITIDAS.stream().anyMatch(uri::startsWith);

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && !esRutaPermitida) {
            HttpSession session = request.getSession(false);
            Boolean verificado = session != null ? (Boolean) session.getAttribute("2fa_verificado") : null;

            if (verificado == null || !verificado) {
                response.sendRedirect(request.getContextPath() + "/verificar-2fa");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
