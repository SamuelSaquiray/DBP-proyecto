package com.recaudia.controller;

import com.recaudia.domain.Factura;
import com.recaudia.repository.FacturaRepository;
import com.recaudia.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController @RequestMapping({"/api/reportes","/api/v1/reportes"}) @RequiredArgsConstructor
public class ReportController {
    private final FacturaRepository facturaRepository;
    @GetMapping(value="/facturas.csv", produces="text/csv")
    public ResponseEntity<byte[]> facturasCsv(){
        List<Factura> facturas=facturaRepository.findByEmpresaId(TenantContext.getTenantId());
        StringBuilder csv=new StringBuilder("id,numero_factura,cuenta,fecha_emision,fecha_vencimiento,monto_total,monto_pagado,saldo,estado\n");
        for(Factura f:facturas){ csv.append(f.getId()).append(',').append(csv(f.getNumeroFactura())).append(',').append(csv(f.getCuenta().getRazonSocial())).append(',')
                .append(f.getFechaEmision()).append(',').append(f.getFechaVencimiento()).append(',').append(f.getMontoTotal()).append(',').append(f.getMontoPagado()).append(',')
                .append(f.getMontoTotal().subtract(f.getMontoPagado())).append(',').append(f.getEstado()).append('\n'); }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=facturas-recaudia.csv").contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
    private String csv(String s){ if(s==null)return ""; return "\""+s.replace("\"","\"\"")+"\""; }
}
