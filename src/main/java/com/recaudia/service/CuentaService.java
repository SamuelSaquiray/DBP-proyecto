package com.recaudia.service;

import com.recaudia.domain.Cuenta;
import com.recaudia.domain.Empresa;
import com.recaudia.domain.EstadoCuenta;
import com.recaudia.dto.CuentaRequest;
import com.recaudia.dto.CuentaResponse;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.repository.EmpresaRepository;
import com.recaudia.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        Long empresaId = TenantContext.getTenantId();
        if (cuentaRepository.existsByEmpresaIdAndCodigoCliente(empresaId, request.getCodigoCliente())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese código de cliente");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));

        Cuenta cuenta = Cuenta.builder()
                .codigoCliente(request.getCodigoCliente())
                .razonSocial(request.getRazonSocial())
                .ruc(request.getRuc())
                .emailContacto(request.getEmailContacto())
                .telefonoWhatsApp(request.getTelefonoWhatsApp())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoCuenta.ACTIVA)
                .empresa(empresa)
                .build();

        cuenta = cuentaRepository.save(cuenta);
        return toResponse(cuenta);
    }

    @Transactional(readOnly = true)
    public List<CuentaResponse> listar() {
        Long empresaId = TenantContext.getTenantId();
        return cuentaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CuentaResponse obtenerPorId(Long cuentaId) {
        Long empresaId = TenantContext.getTenantId();
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        if (!cuenta.getEmpresa().getId().equals(empresaId)) {
            throw new EntityNotFoundException("Cuenta no pertenece a la empresa");
        }

        return toResponse(cuenta);
    }

    @Transactional
    public CuentaResponse actualizar(Long cuentaId, CuentaRequest request) {
        Long empresaId=TenantContext.getTenantId();
        Cuenta c=cuentaRepository.findById(cuentaId).orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));
        if(!c.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Cuenta no pertenece a la empresa");
        c.setRazonSocial(request.getRazonSocial()); c.setRuc(request.getRuc()); c.setEmailContacto(request.getEmailContacto()); c.setTelefonoWhatsApp(request.getTelefonoWhatsApp());
        if(request.getEstado()!=null) c.setEstado(request.getEstado());
        return toResponse(cuentaRepository.save(c));
    }

    @Transactional
    public void cancelar(Long cuentaId) {
        Long empresaId=TenantContext.getTenantId(); Cuenta c=cuentaRepository.findById(cuentaId).orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));
        if(!c.getEmpresa().getId().equals(empresaId)) throw new EntityNotFoundException("Cuenta no pertenece a la empresa");
        c.setEstado(EstadoCuenta.CANCELADA); cuentaRepository.save(c);
    }

    private CuentaResponse toResponse(Cuenta c) {
        return CuentaResponse.builder()
                .id(c.getId())
                .codigoCliente(c.getCodigoCliente())
                .razonSocial(c.getRazonSocial())
                .ruc(c.getRuc())
                .emailContacto(c.getEmailContacto())
                .telefonoWhatsApp(c.getTelefonoWhatsApp())
                .estado(c.getEstado())
                .empresaId(c.getEmpresa().getId())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
