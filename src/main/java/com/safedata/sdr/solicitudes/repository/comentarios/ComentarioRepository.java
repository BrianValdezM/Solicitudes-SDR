package com.safedata.sdr.solicitudes.repository.comentarios;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.model.comentarios.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer>,
                                              JpaSpecificationExecutor<Comentario> {

    Page<Comentario> findByUsuarioOrderByFechaDesc(Usuario usuario, Pageable pageable);

    Page<Comentario> findByUsuarioAndFolioOrderByFechaDesc(Usuario usuario, String folio, Pageable pageable);

    Page<Comentario> findAllByOrderByFechaDesc(Pageable pageable);

    Page<Comentario> findByFolioOrderByFechaDesc(String folio, Pageable pageable);
}