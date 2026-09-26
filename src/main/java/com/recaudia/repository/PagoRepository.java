package com.recaudia.repository;

import com.recaudia.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFacturaId(Long facturaId);

    List<Pago> findByEmpresaId(Long empresaId);

    List<Pago> findByEmpresaIdAndFechaPagoBetween(
            Long empresaId, LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.empresa.id = :empresaId")
    BigDecimal sumMontoByEmpresaId(@Param("empresaId") Long empresaId);
}
