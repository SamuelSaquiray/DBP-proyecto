package com.recaudia.repository;

import com.recaudia.domain.ServicioContratado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioContratadoRepository extends JpaRepository<ServicioContratado, Long> {

    List<ServicioContratado> findByCuentaId(Long cuentaId);

    List<ServicioContratado> findByCuentaIdAndActivoTrue(Long cuentaId);

    List<ServicioContratado> findByServicioId(Long servicioId);
}
