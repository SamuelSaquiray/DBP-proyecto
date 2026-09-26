package com.recaudia.event;

import com.recaudia.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessEventListener {
    private final EmailService emailService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFacturaCreated(FacturaCreatedEvent event) {
        log.info("FacturaCreatedEvent factura={} empresa={}", event.facturaId(), event.empresaId());
        if (event.recipientEmail() != null && !event.recipientEmail().isBlank()) {
            emailService.sendInvoiceNotification(event.recipientEmail(), event.numeroFactura(), event.facturaId());
        }
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPagoRegistered(PagoRegisteredEvent event) {
        log.info("PagoRegisteredEvent pago={} factura={} empresa={} monto={}",
                event.pagoId(), event.facturaId(), event.empresaId(), event.monto());
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAlertaStatusChanged(AlertaStatusChangedEvent event) {
        log.info("AlertaStatusChangedEvent alerta={} empresa={} estado={}",
                event.alertaId(), event.empresaId(), event.estado());
    }
}
