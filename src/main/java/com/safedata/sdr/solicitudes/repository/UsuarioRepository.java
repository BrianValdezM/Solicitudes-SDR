package com.safedata.sdr.solicitudes.repository;

import com.safedata.sdr.solicitudes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    long countByRolAndActivoTrue(String rol);   // para proteger al último admin
}