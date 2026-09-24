package com.safedata.sdr.solicitudes.service;

import com.safedata.sdr.solicitudes.model.Usuario;
import com.safedata.sdr.solicitudes.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        boolean cuentaNoBloqueada = usuario.getBloqueadoHasta() == null
                || usuario.getBloqueadoHasta().isBefore(java.time.LocalDateTime.now());

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .disabled(!usuario.isActivo())
                .accountLocked(!cuentaNoBloqueada)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())))
                .build();
    }
}
