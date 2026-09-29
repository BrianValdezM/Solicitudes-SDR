package com.safedata.sdr.solicitudes.repository;

import com.safedata.sdr.solicitudes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>,
                                            JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    long countByRolAndActivoTrue(String rol);
}