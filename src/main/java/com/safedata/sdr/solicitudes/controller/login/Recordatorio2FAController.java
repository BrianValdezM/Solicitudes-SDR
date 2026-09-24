package com.safedata.sdr.solicitudes.controller.login;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.login.AutenticacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Recordatorio2FAController {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacionService autenticacionService;

    public Recordatorio2FAController(UsuarioRepository usuarioRepository,
                                     AutenticacionService autenticacionService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacionService = autenticacionService;
    }

    @GetMapping("/recordatorio-2fa")
    public String mostrar(HttpSession session) {
        Usuario usuario = obtenerUsuarioDeSesion(session);
        // Si ya lo activó, no tiene sentido mostrar la oferta
        if (usuario.isDosFactoresActivo()) {
            return "redirect:/solicitudes";
        }
        return "login/recordatorio-2fa";
    }

    @PostMapping("/recordatorio-2fa")
    public String decidir(@RequestParam String decision, HttpSession session) {
        Usuario usuario = obtenerUsuarioDeSesion(session);

        if ("si".equals(decision)) {
            // Va directo al setup. /verificar-2fa detectará que no está activo
            // y mostrará el QR + formulario de confirmación.
            return "redirect:/verificar-2fa";
        }

        // Dijo "no": guardamos la fecha y lo dejamos entrar.
        autenticacionService.registrarRecordatorio2fa(usuario);
        return "redirect:/solicitudes";
    }

    private Usuario obtenerUsuarioDeSesion(HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) throw new IllegalStateException("Sesión inválida");
        return usuarioRepository.findById(usuarioId).orElseThrow();
    }
}