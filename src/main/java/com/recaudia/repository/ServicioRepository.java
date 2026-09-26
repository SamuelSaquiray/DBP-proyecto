package com.recaudia.repository;

import com.recaudia.domain.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findByEmpresaIdOrEmpresaIsNull(Long empresaId);

    List<Servicio> findByEmpresaIdAndActivoTrue(Long empresaId);

    Optional<Servicio> findByCodigoAndEmpresaId(String codigo, Long empresaId);

    Optional<Servicio> findByCodigoAndEmpresaIsNull(String codigo);
}
