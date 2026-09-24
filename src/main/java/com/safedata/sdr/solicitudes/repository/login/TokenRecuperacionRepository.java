package com.safedata.sdr.solicitudes.repository.login;

import com.safedata.sdr.solicitudes.model.login.TokenRecuperacion;
import com.safedata.sdr.solicitudes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {
    Optional<TokenRecuperacion> findByToken(String token);
    List<TokenRecuperacion> findByUsuario(Usuario usuario);
}