package logistica.dto;

public record CamionDTO(
    String patente,
    double volumen,
    double altura,
    double cargaMax
) {}
