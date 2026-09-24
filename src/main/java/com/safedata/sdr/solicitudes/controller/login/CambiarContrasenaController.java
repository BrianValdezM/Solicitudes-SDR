package com.safedata.sdr.solicitudes.controller.login;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.login.AutenticacionService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CambiarContrasenaController {

    private static final String REGEX_CONTRASENA_SEGURA =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    private final UsuarioRepository usuarioRepository;
    private final AutenticacionService autenticacionService;
    private final PasswordEncoder passwordEncoder;

    public CambiarContrasenaController(UsuarioRepository usuarioRepository,
                                        AutenticacionService autenticacionService,
                                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacionService = autenticacionService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cambiar-contrasena")
    public String mostrarFormulario(@RequestParam(required = false) String obligatorio, Model model) {
        model.addAttribute("obligatorio", obligatorio != null);
        return "login/cambiar-contrasena";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiar(@RequestParam String contrasenaActual,
                           @RequestParam String contrasenaNueva,
                           @RequestParam String confirmarContrasena,
                           HttpSession session, Model model) {

    	Integer usuarioId = (Integer) session.getAttribute("usuarioId");
    	if (usuarioId == null) {
    	    return "redirect:/login";
    	}
    	Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasenaHash())) {
            model.addAttribute("mensajeError", "La contraseña actual no es correcta.");
            return "login/cambiar-contrasena";
        }
        if (!contrasenaNueva.equals(confirmarContrasena)) {
            model.addAttribute("mensajeError", "Las contraseñas nuevas no coinciden.");
            return "login/cambiar-contrasena";
        }
        if (!contrasenaNueva.matches(REGEX_CONTRASENA_SEGURA)) {
            model.addAttribute("mensajeError",
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
            return "login/cambiar-contrasena";
        }

        autenticacionService.cambiarContrasena(usuario, contrasenaNueva);

        if (usuario.isDosFactoresActivo()) {
            session.setAttribute("2fa_verificado", false);
            return "redirect:/verificar-2fa";
        }
        session.setAttribute("2fa_verificado", true);
        return "redirect:/solicitudes";
    }
}
