package com.recaudia.repository;

import com.recaudia.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByEmpresaIdOrderByFechaDesc(Long empresaId);
    List<AuditLog> findTop100ByEmpresaIdAndEntidadAndEntidadIdOrderByFechaDesc(Long empresaId, String entidad, Long entidadId);
}
