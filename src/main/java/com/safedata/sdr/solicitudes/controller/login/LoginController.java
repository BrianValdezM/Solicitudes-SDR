package com.safedata.sdr.solicitudes.controller.login;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String mostrarLogin(Model model,
                                @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
                                @org.springframework.web.bind.annotation.RequestParam(required = false) String salio) {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return "redirect:/solicitudes";
        }

        if ("credenciales".equals(error)) {
            model.addAttribute("mensajeError", "Correo o contraseña incorrectos.");
        } else if ("bloqueada".equals(error)) {
            model.addAttribute("mensajeError", "Tu cuenta se bloqueó temporalmente por varios intentos fallidos. Intenta más tarde.");
        }
        if (salio != null) {
            model.addAttribute("mensajeInfo", "Sesión cerrada correctamente.");
        }
        return "login/login";
    }

    @GetMapping("/solicitudes")
    public String dashboard(Model model, HttpSession session) {
        model.addAttribute("nombreCompleto", session.getAttribute("nombreCompleto"));
        return "dashboard";
    }
}
