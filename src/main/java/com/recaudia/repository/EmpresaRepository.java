package com.recaudia.repository;

import com.recaudia.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRuc(String ruc);

    Optional<Empresa> findByDominio(String dominio);

    boolean existsByRuc(String ruc);

    boolean existsByDominio(String dominio);
}
