package com.recaudia.service;

import com.recaudia.domain.Cuenta;
import com.recaudia.domain.Servicio;
import com.recaudia.domain.ServicioContratado;
import com.recaudia.dto.ServicioContratadoRequest;
import com.recaudia.dto.ServicioContratadoResponse;
import com.recaudia.repository.CuentaRepository;
import com.recaudia.repository.ServicioContratadoRepository;
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
public class ServicioContratadoService {

    private final ServicioContratadoRepository servicioContratadoRepository;
    private final CuentaRepository cuentaRepository;
    private final ServicioRepository servicioRepository;

    @Transactional
    public ServicioContratadoResponse contratar(Long cuentaId, ServicioContratadoRequest request) {
        Long empresaId = TenantContext.getTenantId();

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta no encontrada"));

        if (!cuenta.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("La cuenta no pertenece a la empresa");
        }

        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

        ServicioContratado contratado = ServicioContratado.builder()
                .cuenta(cuenta)
                .servicio(servicio)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .tarifaMensual(request.getTarifaMensual())
                .activo(true)
                .build();

        contratado = servicioContratadoRepository.save(contratado);
        return toResponse(contratado);
    }

    @Transactional(readOnly = true)
    public List<ServicioContratadoResponse> listarPorCuenta(Long cuentaId) {
        return servicioContratadoRepository.findByCuentaIdAndActivoTrue(cuentaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ServicioContratadoResponse toResponse(ServicioContratado sc) {
        return ServicioContratadoResponse.builder()
                .id(sc.getId())
                .cuentaId(sc.getCuenta().getId())
                .servicioId(sc.getServicio().getId())
                .servicioNombre(sc.getServicio().getNombre())
                .fechaInicio(sc.getFechaInicio())
                .fechaFin(sc.getFechaFin())
                .tarifaMensual(sc.getTarifaMensual())
                .activo(sc.getActivo())
                .build();
    }
}
