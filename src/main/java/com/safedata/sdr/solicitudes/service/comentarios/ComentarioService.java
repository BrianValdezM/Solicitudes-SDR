package com.safedata.sdr.solicitudes.service.comentarios;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.model.comentarios.Comentario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import com.safedata.sdr.solicitudes.repository.comentarios.ComentarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComentarioService {

    private static final int POR_PAGINA = 12;

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista comentarios paginados.
     * @param folioFiltro  opcional
     * @param usuarioId    usuario logueado
     * @param esAdmin      true si el rol es ADMIN (ve todos)
     * @param pagina       página (base 0)
     */
    public Page<Comentario> listar(String folioFiltro, Integer usuarioId,
                                    boolean esAdmin, int pagina) {

        Pageable pageable = PageRequest.of(Math.max(0, pagina), POR_PAGINA);

        if (esAdmin) {
            if (folioFiltro != null && !folioFiltro.isBlank()) {
                return comentarioRepository.findByFolioOrderByFechaDesc(folioFiltro.trim(), pageable);
            }
            return comentarioRepository.findAllByOrderByFechaDesc(pageable);
        }

        // Usuario normal: solo sus propios comentarios
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        if (folioFiltro != null && !folioFiltro.isBlank()) {
            return comentarioRepository.findByUsuarioAndFolioOrderByFechaDesc(
                    usuario, folioFiltro.trim(), pageable);
        }
        return comentarioRepository.findByUsuarioOrderByFechaDesc(usuario, pageable);
    }

    @Transactional
    public Comentario agregar(String folio, String texto, Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Comentario c = new Comentario();
        c.setFolio(folio.trim());
        c.setComentario(texto.trim());
        c.setUsuario(usuario);
        c.setFecha(LocalDateTime.now());
        return comentarioRepository.save(c);
    }

    /** Actualiza solo si el dueño es el mismo (o es admin). */
    @Transactional
    public Comentario actualizar(Integer id, String folio, String texto,
                                  Integer usuarioId, boolean esAdmin) {
        Comentario c = comentarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado."));

        if (!esAdmin && !c.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes editar un comentario que no es tuyo.");
        }

        c.setFolio(folio.trim());
        c.setComentario(texto.trim());
        return comentarioRepository.save(c);
    }

    /** Elimina solo si el dueño es el mismo (o es admin). */
    @Transactional
    public void eliminar(Integer id, Integer usuarioId, boolean esAdmin) {
        Comentario c = comentarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado."));

        if (!esAdmin && !c.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes eliminar un comentario que no es tuyo.");
        }
        comentarioRepository.delete(c);
    }

    /** Para cargar el comentario al editar; valida dueño. */
    public Comentario obtenerParaEditar(Integer id, Integer usuarioId, boolean esAdmin) {
        Comentario c = comentarioRepository.findById(id).orElse(null);
        if (c == null) return null;
        if (!esAdmin && !c.getUsuario().getId().equals(usuarioId)) return null;
        return c;
    }
}