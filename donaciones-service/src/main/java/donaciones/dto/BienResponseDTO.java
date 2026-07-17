package donaciones.dto;

public record BienResponseDTO(
    String subcategoria,
    Integer cantidad,
    String unidad,
    String estado,
    String vencimiento,
    String descripcion,
    String foto
) {}
