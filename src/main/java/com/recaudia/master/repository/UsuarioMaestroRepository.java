package com.recaudia.master.repository;

import com.recaudia.domain.UsuarioMaestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioMaestroRepository extends JpaRepository<UsuarioMaestro, Long> {
    Optional<UsuarioMaestro> findByEmail(String email);
    boolean existsByEmail(String email);
}
