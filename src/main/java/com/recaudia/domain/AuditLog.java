package com.recaudia.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_empresa_fecha", columnList = "empresa_id,fecha"),
        @Index(name = "idx_audit_entidad", columnList = "entidad,entidad_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String accion;
    @Column(nullable = false) private String entidad;
    @Column(name = "entidad_id") private Long entidadId;
    @Column(name = "usuario_id") private Long usuarioId;
    private String usuarioEmail;
    @Column(columnDefinition = "TEXT") private String detalle;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, updatable = false) private LocalDateTime fecha;
    @PrePersist protected void onCreate(){ if(fecha==null) fecha=LocalDateTime.now(); }
}
