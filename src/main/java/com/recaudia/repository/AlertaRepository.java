package com.recaudia.repository;

import com.recaudia.domain.Alerta;
import com.recaudia.domain.EstadoAlerta;
import com.recaudia.domain.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByEmpresaId(Long empresaId);

    List<Alerta> findByEmpresaIdAndEstado(Long empresaId, EstadoAlerta estado);

    List<Alerta> findByEmpresaIdAndTipo(Long empresaId, TipoAlerta tipo);

    Optional<Alerta> findByFacturaId(Long facturaId);

    List<Alerta> findByRevisoresId(Long usuarioId);

    long countByEmpresaIdAndEstado(Long empresaId, EstadoAlerta estado);
}
