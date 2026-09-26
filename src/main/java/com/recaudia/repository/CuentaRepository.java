package com.recaudia.repository;

import com.recaudia.domain.Cuenta;
import com.recaudia.domain.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByEmpresaId(Long empresaId);

    Optional<Cuenta> findByEmpresaIdAndCodigoCliente(Long empresaId, String codigoCliente);

    List<Cuenta> findByEmpresaIdAndEstado(Long empresaId, EstadoCuenta estado);

    List<Cuenta> findByEmpresaIdAndRazonSocialContainingIgnoreCase(Long empresaId, String razonSocial);

    boolean existsByEmpresaIdAndCodigoCliente(Long empresaId, String codigoCliente);
}
