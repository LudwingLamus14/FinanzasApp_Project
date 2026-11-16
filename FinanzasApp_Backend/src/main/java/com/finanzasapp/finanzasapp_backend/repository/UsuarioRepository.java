package com.finanzasapp.finanzasapp_backend.repository;

import com.finanzasapp.finanzasapp_backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
