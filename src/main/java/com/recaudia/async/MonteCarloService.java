package com.recaudia.async;

import com.recaudia.domain.Factura;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonteCarloService {

    private final FacturaRepository facturaRepository;
    private final Random random = new Random();

    @Async("taskExecutor")
    public CompletableFuture<ProyeccionResult> simularCobranza(Long empresaId, int escenarios) {
        TenantContext.setTenantId(empresaId);
        try {
            log.info("Iniciando simulación Monte Carlo para empresa {} con {} escenarios", empresaId, escenarios);

            List<Factura> facturasPendientes = facturaRepository.findByEmpresaId(empresaId)
                    .stream()
                    .filter(f -> f.getEstado().name().equals("EMITIDA")
                            || f.getEstado().name().equals("PARCIAL")
                            || f.getEstado().name().equals("VENCIDA"))
                    .toList();

            BigDecimal totalPendiente = facturasPendientes.stream()
                    .map(f -> f.getMontoTotal().subtract(f.getMontoPagado()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal suma = BigDecimal.ZERO;
            BigDecimal min = totalPendiente;
            BigDecimal max = BigDecimal.ZERO;

            for (int i = 0; i < escenarios; i++) {
                BigDecimal escenario = BigDecimal.ZERO;
                for (Factura f : facturasPendientes) {
                    BigDecimal pendiente = f.getMontoTotal().subtract(f.getMontoPagado());
                    double probabilidad = 0.4 + (random.nextDouble() * 0.55);
                    escenario = escenario.add(pendiente.multiply(BigDecimal.valueOf(probabilidad)));
                }
                suma = suma.add(escenario);
                if (escenario.compareTo(min) < 0) min = escenario;
                if (escenario.compareTo(max) > 0) max = escenario;
            }

            BigDecimal promedio = suma.divide(BigDecimal.valueOf(escenarios), 2, RoundingMode.HALF_UP);

            ProyeccionResult result = new ProyeccionResult(promedio, min, max, totalPendiente);
            log.info("Simulación finalizada. Promedio esperado: {}", promedio);
            return CompletableFuture.completedFuture(result);
        } finally {
            TenantContext.clear();
        }
    }

    public record ProyeccionResult(BigDecimal promedio, BigDecimal minimo, BigDecimal maximo, BigDecimal totalPendiente) {}
}
