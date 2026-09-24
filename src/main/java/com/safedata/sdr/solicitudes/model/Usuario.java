package com.safedata.sdr.solicitudes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "fecha_cambio_contrasena", nullable = false)
    private LocalDateTime fechaCambioContrasena;

    @Column(name = "debe_cambiar_contrasena", nullable = false)
    private boolean debeCambiarContrasena;

    @Column(name = "secreto_2fa", length = 64)
    private String secreto2fa;

    @Column(name = "dos_factores_activo", nullable = false)
    private boolean dosFactoresActivo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(nullable = false, length = 30)
    private String rol = "USUARIO";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_ultimo_recordatorio_2fa")
    private LocalDateTime fechaUltimoRecordatorio2fa;
    
    @Column(name = "id_cliente", length = 6)
    private String idCliente;

    @PrePersist
    protected void alCrear() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (fechaCambioContrasena == null) fechaCambioContrasena = LocalDateTime.now();
    }

    // ---- Getters y setters ----

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public LocalDateTime getFechaCambioContrasena() { return fechaCambioContrasena; }
    public void setFechaCambioContrasena(LocalDateTime fechaCambioContrasena) { this.fechaCambioContrasena = fechaCambioContrasena; }

    public boolean isDebeCambiarContrasena() { return debeCambiarContrasena; }
    public void setDebeCambiarContrasena(boolean debeCambiarContrasena) { this.debeCambiarContrasena = debeCambiarContrasena; }

    public String getSecreto2fa() { return secreto2fa; }
    public void setSecreto2fa(String secreto2fa) { this.secreto2fa = secreto2fa; }

    public boolean isDosFactoresActivo() { return dosFactoresActivo; }
    public void setDosFactoresActivo(boolean dosFactoresActivo) { this.dosFactoresActivo = dosFactoresActivo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaUltimoRecordatorio2fa() { return fechaUltimoRecordatorio2fa; }
    public void setFechaUltimoRecordatorio2fa(LocalDateTime fechaUltimoRecordatorio2fa) {
        this.fechaUltimoRecordatorio2fa = fechaUltimoRecordatorio2fa;
    }
    
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getNombreCompleto() { return nombre + " " + apellidos; }
}
