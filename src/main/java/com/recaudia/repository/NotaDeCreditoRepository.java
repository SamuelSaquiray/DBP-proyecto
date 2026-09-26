package com.recaudia.repository;

import com.recaudia.domain.NotaDeCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaDeCreditoRepository extends JpaRepository<NotaDeCredito, Long> {

    List<NotaDeCredito> findByFacturaId(Long facturaId);

    List<NotaDeCredito> findByEmpresaId(Long empresaId);

    Optional<NotaDeCredito> findByEmpresaIdAndNumero(Long empresaId, String numero);
}
