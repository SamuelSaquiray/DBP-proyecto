package com.recaudia.repository;

import com.recaudia.domain.EstadoFactura;
import com.recaudia.domain.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByEmpresaId(Long empresaId);

    List<Factura> findByCuentaId(Long cuentaId);

    Optional<Factura> findByEmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura);
    boolean existsByEmpresaIdAndNumeroFactura(Long empresaId, String numeroFactura);

    List<Factura> findByEmpresaIdAndEstado(Long empresaId, EstadoFactura estado);

    List<Factura> findByEmpresaIdAndFechaVencimientoBeforeAndEstadoNot(
            Long empresaId, LocalDate fecha, EstadoFactura estado);

    @Query("SELECT COALESCE(SUM(f.montoTotal), 0) FROM Factura f WHERE f.empresa.id = :empresaId")
    BigDecimal sumMontoTotalByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(f.montoPagado), 0) FROM Factura f WHERE f.empresa.id = :empresaId")
    BigDecimal sumMontoPagadoByEmpresaId(@Param("empresaId") Long empresaId);
}
