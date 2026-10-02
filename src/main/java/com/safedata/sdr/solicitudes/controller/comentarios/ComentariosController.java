package com.safedata.sdr.solicitudes.controller.comentarios;

import com.safedata.sdr.solicitudes.model.comentarios.Comentario;
import com.safedata.sdr.solicitudes.service.comentarios.ComentarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comentarios")
public class ComentariosController {

    private final ComentarioService comentarioService;

    public ComentariosController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public String listado(@RequestParam(required = false) String folio,
                          @RequestParam(required = false) Integer editar,
                          @RequestParam(defaultValue = "0") int page,
                          Model model,
                          HttpSession session,
                          Authentication auth) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        boolean esAdmin = esAdmin(auth);

        Page<Comentario> pagina = comentarioService.listar(folio, usuarioId, esAdmin, page);

        model.addAttribute("comentarios", pagina.getContent());
        model.addAttribute("pagina", pagina);
        model.addAttribute("filtroFolio", folio);
        model.addAttribute("esAdmin", esAdmin);

        if (editar != null) {
            Comentario enEdicion = comentarioService.obtenerParaEditar(editar, usuarioId, esAdmin);
            model.addAttribute("comentarioEnEdicion", enEdicion);
        }

        return "comentarios/lista";
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Integer id,
                          @RequestParam String folio,
                          @RequestParam String comentario,
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session,
                          Authentication auth,
                          RedirectAttributes ra) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        boolean esAdmin = esAdmin(auth);

        try {
            if (id != null) {
                comentarioService.actualizar(id, folio, comentario, usuarioId, esAdmin);
                ra.addFlashAttribute("mensajeExito", "Comentario actualizado correctamente.");
            } else {
                comentarioService.agregar(folio, comentario, usuarioId);
                ra.addFlashAttribute("mensajeExito", "Comentario agregado correctamente.");
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/comentarios?page=" + page;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
    		               @RequestParam(defaultValue = "0") int page,
                           HttpSession session,
                           Authentication auth,
                           RedirectAttributes ra) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        try {
            comentarioService.eliminar(id, usuarioId, esAdmin(auth));
            ra.addFlashAttribute("mensajeExito", "Comentario eliminado.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/comentarios?page=" + page;
    }

    private boolean esAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}