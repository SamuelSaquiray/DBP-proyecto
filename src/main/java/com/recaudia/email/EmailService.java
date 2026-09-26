package com.recaudia.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) { this.mailSenderProvider = mailSenderProvider; }

    public void sendInvoiceNotification(String to, String invoiceNumber, Long invoiceId) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || to == null || to.isBlank()) {
            log.warn("SMTP no configurado o destinatario vacío; email omitido para factura {}", invoiceId);
            return;
        }
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Nueva factura - Recaud.IA");
            String template = StreamUtils.copyToString(
                    new ClassPathResource("templates/invoice-notification.html").getInputStream(),
                    StandardCharsets.UTF_8);
            helper.setText(String.format(template, invoiceNumber, invoiceId), true);
            sender.send(message);
            log.info("Email enviado a {} para factura {}", to, invoiceId);
        } catch (Exception ex) {
            log.error("No se pudo enviar email para factura {}", invoiceId, ex);
        }
    }
}
