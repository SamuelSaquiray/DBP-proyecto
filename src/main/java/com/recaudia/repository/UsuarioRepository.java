package com.recaudia.repository;

import com.recaudia.domain.Rol;
import com.recaudia.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByEmpresaId(Long empresaId);

    List<Usuario> findByEmpresaIdAndRol(Long empresaId, Rol rol);

    List<Usuario> findByEmpresaIdAndActivoTrue(Long empresaId);

    boolean existsByEmail(String email);
}
