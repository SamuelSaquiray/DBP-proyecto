package com.recaudia.service;

import com.recaudia.domain.AuditLog;
import com.recaudia.dto.AuditLogResponse;
import com.recaudia.domain.Empresa;
import com.recaudia.domain.Usuario;
import com.recaudia.repository.AuditLogRepository;
import com.recaudia.repository.EmpresaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public void log(String accion, String entidad, Long entidadId, String detalle) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) return;
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long usuarioId = null; String email = null;
        if (auth != null && auth.getPrincipal() instanceof Usuario u) { usuarioId=u.getId(); email=u.getEmail(); }
        repository.save(AuditLog.builder().accion(accion).entidad(entidad).entidadId(entidadId)
                .usuarioId(usuarioId).usuarioEmail(email).detalle(detalle).empresa(empresa).build());
    }

    @Transactional(readOnly=true)
    public List<AuditLogResponse> listar() { return repository.findTop200ByEmpresaIdOrderByFechaDesc(TenantContext.getTenantId()).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true)
    public List<AuditLogResponse> historial(String entidad, Long entidadId) {
        return repository.findTop100ByEmpresaIdAndEntidadAndEntidadIdOrderByFechaDesc(TenantContext.getTenantId(), entidad, entidadId).stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog a) { return AuditLogResponse.builder().id(a.getId()).accion(a.getAccion()).entidad(a.getEntidad()).entidadId(a.getEntidadId()).usuarioId(a.getUsuarioId()).usuarioEmail(a.getUsuarioEmail()).detalle(a.getDetalle()).fecha(a.getFecha()).build(); }
}
