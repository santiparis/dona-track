package donaciones.dto;

public record BienDTO(
        String nombreSubcategoria,
        Integer cantidad,
        String unidad,
        String descripcion
) {}