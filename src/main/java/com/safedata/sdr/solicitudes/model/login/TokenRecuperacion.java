package com.safedata.sdr.solicitudes.model.login;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.safedata.sdr.solicitudes.model.Usuario;

@Entity
@Table(name = "tokens_recuperacion")
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_expira", nullable = false)
    private LocalDateTime fechaExpira;

    @Column(nullable = false)
    private boolean usado = false;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaExpira() { return fechaExpira; }
    public void setFechaExpira(LocalDateTime fechaExpira) { this.fechaExpira = fechaExpira; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public boolean estaVigente() {
        return !usado && LocalDateTime.now().isBefore(fechaExpira);
    }
}
