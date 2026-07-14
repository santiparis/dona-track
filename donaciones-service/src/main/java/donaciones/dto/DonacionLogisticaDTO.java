package donaciones.dto;

public record DonacionLogisticaDTO(
    String donacionID,
    int cantidadBienes,
    String unidad,
    String destino,
    String entidadNombre
) {}