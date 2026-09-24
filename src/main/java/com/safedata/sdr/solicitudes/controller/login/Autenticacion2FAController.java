package com.safedata.sdr.solicitudes.controller.login;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.login.AutenticacionService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Autenticacion2FAController {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacionService autenticacionService;

    public Autenticacion2FAController(UsuarioRepository usuarioRepository, AutenticacionService autenticacionService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacionService = autenticacionService;
    }

    @GetMapping("/verificar-2fa")
    public String mostrarFormulario(HttpSession session, Model model) {
        Usuario usuario = obtenerUsuarioDeSesion(session);

        if (!usuario.isDosFactoresActivo()) {
            // Reusa el secreto existente si lo hay; solo genera uno nuevo si nunca se le generó
            String urlOtpAuth;
            if (usuario.getSecreto2fa() == null) {
                urlOtpAuth = autenticacionService.iniciarConfiguracion2fa(usuario);
            } else {
                urlOtpAuth = autenticacionService.generarUrlOtpAuth(usuario);
            }
            model.addAttribute("urlOtpAuth", urlOtpAuth);
            model.addAttribute("configurando", true);
        }
        return "login/verificar-2fa";
    }

    @PostMapping("/verificar-2fa")
    public String verificarCodigo(@RequestParam String codigo, HttpSession session, Model model) {
        Usuario usuario = obtenerUsuarioDeSesion(session);

        boolean valido;
        if (!usuario.isDosFactoresActivo()) {
            // Primera vez: confirma la activación
            valido = autenticacionService.confirmarActivacion2fa(usuario, codigo);
        } else {
            valido = autenticacionService.validarCodigo2fa(usuario, codigo);
        }

        if (!valido) {
            model.addAttribute("mensajeError", "Código incorrecto. Intenta de nuevo.");
            if (usuario.getSecreto2fa() != null && !usuario.isDosFactoresActivo()) {
                model.addAttribute("configurando", true);
            }
            return "login/verificar-2fa";
        }

        session.setAttribute("2fa_verificado", true);
        return "redirect:/solicitudes";
    }

    private Usuario obtenerUsuarioDeSesion(HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) throw new IllegalStateException("Sesión inválida");
        return usuarioRepository.findById(usuarioId).orElseThrow();
    }
}
