package logistica.dto;

import logistica.domain.EstadoCamion;

public record CamionDTO(
    String patente,
    double volumen,
    double altura,
    double cargaMax,
    EstadoCamion estado
) {}
