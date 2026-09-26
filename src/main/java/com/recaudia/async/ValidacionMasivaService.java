package com.recaudia.async;

import com.recaudia.domain.Alerta;
import com.recaudia.domain.EstadoAlerta;
import com.recaudia.domain.Factura;
import com.recaudia.domain.TipoAlerta;
import com.recaudia.repository.AlertaRepository;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidacionMasivaService {

    private final FacturaRepository facturaRepository;
    private final AlertaRepository alertaRepository;
    private final com.recaudia.service.AuditService auditService;

    @Async("taskExecutor")
    public void validarFacturasMasivas(Long empresaId) {
        TenantContext.setTenantId(empresaId);
        try {
            log.info("Iniciando validación masiva para empresa {}", empresaId);

            List<Factura> facturas = facturaRepository.findByEmpresaId(empresaId);

            for (Factura factura : facturas) {
                if (factura.getMontoTotal().signum() <= 0 && alertaRepository.findByFacturaId(factura.getId()).isEmpty()) {
                    Alerta alerta = Alerta.builder()
                            .titulo("Factura con monto inválido")
                            .descripcion("La factura " + factura.getNumeroFactura() + " tiene monto <= 0")
                            .tipo(TipoAlerta.INCONSISTENCIA_DATOS)
                            .estado(EstadoAlerta.PENDIENTE)
                            .factura(factura)
                            .empresa(factura.getEmpresa())
                            .build();
                    alertaRepository.save(alerta);
                }
            }

            auditService.log("VALIDACION_MASIVA", "FACTURA", null, "Validación masiva completada");
            log.info("Validación masiva finalizada para empresa {}", empresaId);
        } finally {
            TenantContext.clear();
        }
    }
}
