package com.safedata.sdr.solicitudes.controller.login;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.safedata.sdr.solicitudes.service.login.RecuperacionService;

@Controller
public class RecuperacionController {

    private static final String REGEX_CONTRASENA_SEGURA =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    private final RecuperacionService recuperacionService;

    public RecuperacionController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @GetMapping("/olvide-contrasena")
    public String mostrarFormularioSolicitud() {
        return "login/olvide-contrasena";
    }

    @PostMapping("/olvide-contrasena")
    public String solicitar(@RequestParam String correo, Model model) {
        recuperacionService.solicitarRecuperacion(correo);
        // Mismo mensaje exista o no la cuenta, por seguridad
        model.addAttribute("mensajeInfo",
                "Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.");
        return "login/olvide-contrasena";
    }

    @GetMapping("/restablecer-contrasena")
    public String mostrarFormularioRestablecer(@RequestParam String token, Model model) {
        boolean valido = recuperacionService.validarToken(token).isPresent();
        model.addAttribute("tokenValido", valido);
        model.addAttribute("token", token);
        return "login/restablecer-contrasena";
    }

    @PostMapping("/restablecer-contrasena")
    public String restablecer(@RequestParam String token,
                               @RequestParam String contrasenaNueva,
                               @RequestParam String confirmarContrasena,
                               Model model) {

        if (!contrasenaNueva.equals(confirmarContrasena)) {
            model.addAttribute("mensajeError", "Las contraseñas no coinciden.");
            model.addAttribute("tokenValido", true);
            model.addAttribute("token", token);
            return "login/restablecer-contrasena";
        }
        if (!contrasenaNueva.matches(REGEX_CONTRASENA_SEGURA)) {
            model.addAttribute("mensajeError",
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.");
            model.addAttribute("tokenValido", true);
            model.addAttribute("token", token);
            return "login/restablecer-contrasena";
        }

        boolean exito = recuperacionService.restablecerContrasena(token, contrasenaNueva);
        if (!exito) {
            model.addAttribute("tokenValido", false);
            return "login/restablecer-contrasena";
        }

        model.addAttribute("restablecidoConExito", true);
        return "login/restablecer-contrasena";
    }
}
