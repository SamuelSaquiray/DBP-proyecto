package com.recaudia.event;
public record FacturaCreatedEvent(Long facturaId, Long empresaId, String numeroFactura, String recipientEmail) {}
