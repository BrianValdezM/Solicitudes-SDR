package com.safedata.sdr.solicitudes.controller;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.service.UsuarioAdminService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioAdminService service;
    private final UsuarioRepository usuarioRepository;

    public AdminUsuarioController(UsuarioAdminService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    // ---- Listado ----
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/usuarios/lista";
    }

    // ---- Crear ----
    @GetMapping("/nuevo")
    public String formularioNuevo() {
        return "admin/usuarios/nuevo";
    }

    @PostMapping
    public String crear(@RequestParam String nombre,
                        @RequestParam String apellidos,
                        @RequestParam String correo,
                        @RequestParam(required = false) String telefono,
                        @RequestParam(required = false) String idCliente,
                        @RequestParam(required = false) String rol,
                        Model model) {
        try {
            service.crearUsuario(nombre, apellidos, correo, telefono, idCliente, rol);
            return "redirect:/admin/usuarios?creado";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellidos", apellidos);
            model.addAttribute("correo", correo);
            model.addAttribute("telefono", telefono);
            model.addAttribute("idCliente", idCliente);
            return "admin/usuarios/nuevo";
        }
    }

    // ---- Editar ----
    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        model.addAttribute("usuario", usuario);
        return "admin/usuarios/editar";
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Integer id,
                             @RequestParam String nombre,
                             @RequestParam String apellidos,
                             @RequestParam String correo,
                             @RequestParam(required = false) String telefono,
                             @RequestParam(required = false) String idCliente,
                             @RequestParam(required = false) String rol,
                             @RequestParam(required = false) String resetearContrasena,
                             Model model) {
        boolean resetear = "true".equals(resetearContrasena);

        try {
            service.actualizarUsuario(id, nombre, apellidos, correo,
                                      telefono, idCliente, rol, resetear);
            return "redirect:/admin/usuarios?editado" + (resetear ? "&reseteado" : "");
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeError", e.getMessage());
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
            model.addAttribute("usuario", usuario);
            return "admin/usuarios/editar";
        }
    }
    
 // ---- Eliminar ----
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        Integer adminId = (Integer) session.getAttribute("usuarioId");
        try {
            service.eliminarUsuario(id, adminId);
            ra.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ---- Cambiar estado ----
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        Integer adminId = (Integer) session.getAttribute("usuarioId");
        try {
            service.cambiarEstado(id, adminId);
            ra.addFlashAttribute("mensajeExito", "Estado actualizado.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}