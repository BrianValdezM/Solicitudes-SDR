package com.safedata.sdr.solicitudes.repository.spec;

import com.safedata.sdr.solicitudes.model.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {}

    public static Specification<Usuario> conFiltros(String nombre,
                                                     String apellidos,
                                                     String correo,
                                                     String idCliente,
                                                     Boolean activo,
                                                     String rol) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (nombre != null && !nombre.isBlank()) {
                predicados.add(cb.like(
                        cb.lower(root.get("nombre")),
                        "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (apellidos != null && !apellidos.isBlank()) {
                predicados.add(cb.like(
                        cb.lower(root.get("apellidos")),
                        "%" + apellidos.trim().toLowerCase() + "%"));
            }
            if (correo != null && !correo.isBlank()) {          // ← NUEVO
                predicados.add(cb.like(
                        cb.lower(root.get("correo")),
                        "%" + correo.trim().toLowerCase() + "%"));
            }
            if (idCliente != null && !idCliente.isBlank()) {
                predicados.add(cb.like(
                        cb.lower(root.get("idCliente")),
                        "%" + idCliente.trim().toLowerCase() + "%"));
            }
            if (activo != null) {
                predicados.add(cb.equal(root.get("activo"), activo));
            }
            if (rol != null && !rol.isBlank()) {
                predicados.add(cb.equal(root.get("rol"), rol));
            }

            // Orden por defecto: más recientes primero
            if (query != null) {
                query.orderBy(cb.desc(root.get("fechaCreacion")));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}