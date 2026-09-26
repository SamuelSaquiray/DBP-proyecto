package com.recaudia.event;
import com.recaudia.domain.EstadoAlerta;
public record AlertaStatusChangedEvent(Long alertaId, Long empresaId, EstadoAlerta estado) {}
