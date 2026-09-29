package com.safedata.sdr.solicitudes.controller.solicitudes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudesController {

    /** Tarjetas: Solicitudes / Envíos / Recolecciones. */
    @GetMapping
    public String dashboard() {
        return "solicitudes/dashboard";
    }
}