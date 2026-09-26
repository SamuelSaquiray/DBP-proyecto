package com.recaudia.service;

import com.recaudia.domain.Empresa;
import com.recaudia.domain.Servicio;
import com.recaudia.dto.ServicioRequest;
import com.recaudia.dto.ServicioResponse;
import com.recaudia.repository.EmpresaRepository;
import com.recaudia.repository.ServicioRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Long empresaId = TenantContext.getTenantId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Servicio servicio = Servicio.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .empresa(empresa)
                .activo(true)
                .build();

        servicio = servicioRepository.save(servicio);
        return toResponse(servicio);
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar() {
        Long empresaId = TenantContext.getTenantId();
        return servicioRepository.findByEmpresaIdOrEmpresaIsNull(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicioResponse actualizar(Long id, ServicioRequest request) {
        Long empresaId=TenantContext.getTenantId(); Servicio s=servicioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));
        if(s.getEmpresa()!=null && !s.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Servicio no pertenece a la empresa");
        s.setCodigo(request.getCodigo()); s.setNombre(request.getNombre()); s.setDescripcion(request.getDescripcion());
        return toResponse(servicioRepository.save(s));
    }
    @Transactional public void desactivar(Long id) { Long empresaId=TenantContext.getTenantId(); Servicio s=servicioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado")); if(s.getEmpresa()!=null && !s.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Servicio no pertenece a la empresa"); s.setActivo(false); servicioRepository.save(s); }

    private ServicioResponse toResponse(Servicio s) {
        return ServicioResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .activo(s.getActivo())
                .empresaId(s.getEmpresa() != null ? s.getEmpresa().getId() : null)
                .build();
    }
}
