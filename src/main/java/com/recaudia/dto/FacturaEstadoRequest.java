package com.recaudia.dto;
import com.recaudia.domain.EstadoFactura;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class FacturaEstadoRequest { @NotNull private EstadoFactura estado; }
