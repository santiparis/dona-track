package donaciones.dto;

import java.util.Map;

public record NecesidadDTO(
    String descripcion,
    boolean renovacion,
    Map<String, Integer> cantidadesRequeridas,
    String fechaInicio,
    String periodo
) {}
