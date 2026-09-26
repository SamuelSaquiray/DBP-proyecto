package com.recaudia.async;

import com.recaudia.domain.EstadoFactura;
import com.recaudia.domain.Factura;
import com.recaudia.repository.FacturaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonteCarloServiceTest {
    @Test void calculaProyeccionNoNegativa() {
        FacturaRepository repo=Mockito.mock(FacturaRepository.class);
        Factura f=Factura.builder().id(1L).montoTotal(new BigDecimal("100")).montoPagado(new BigDecimal("20"))
                .estado(EstadoFactura.EMITIDA).fechaVencimiento(LocalDate.now().plusDays(5)).build();
        Mockito.when(repo.findByEmpresaId(1L)).thenReturn(List.of(f));
        MonteCarloService service=new MonteCarloService(repo);
        var result=service.simularCobranza(1L,100).join();
        assertTrue(result.promedio().compareTo(BigDecimal.ZERO)>=0);
        assertEquals(0, new BigDecimal("80.00").compareTo(result.totalPendiente()));
        assertTrue(result.maximo().compareTo(result.minimo())>=0);
    }
}
