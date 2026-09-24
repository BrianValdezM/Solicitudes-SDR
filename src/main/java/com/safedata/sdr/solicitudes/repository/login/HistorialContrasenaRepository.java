package com.safedata.sdr.solicitudes.repository.login;

import com.safedata.sdr.solicitudes.model.login.HistorialContrasena;
import com.safedata.sdr.solicitudes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialContrasenaRepository extends JpaRepository<HistorialContrasena, Integer> {
    List<HistorialContrasena> findByUsuario(Usuario usuario);
}